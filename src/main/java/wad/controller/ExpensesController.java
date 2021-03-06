package wad.controller;

import java.util.Date;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;
import wad.domain.Comment;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.CommentRepository;
import wad.repository.ReceiptRepository;
import wad.service.ExpenseService;
import wad.util.PagingHelper;
import wad.validator.ExpenseValidator;

@Controller
@RequestMapping("/expenses")
@SessionAttributes("expense")
public class ExpensesController {

    private final static int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    @Qualifier("expenseValidator")
    ExpenseValidator expenseValidator;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
        dataBinder.setDisallowedFields("user");
        dataBinder.setDisallowedFields("status");
    }

    @InitBinder("expense")
    public void expenseInitBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(expenseValidator);
    }

    @ModelAttribute("expense")
    private Expense getExpense() {
        return new Expense();
    }

    @ModelAttribute("expenseRow")
    private ExpenseRow getExpenseRow() {
        return new ExpenseRow();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String listExpenses(Model model, @RequestParam(value = "status", required = false) Expense.Status status, @RequestParam(required = false) Integer pageNumber) {

        Page<Expense> page = null;
        if (status == null) {
            page = expenseService.getPagedExpenses(pageNumber, DEFAULT_PAGE_SIZE);
        } else {
            model.addAttribute("status", status.toString());
            page = expenseService.getPagedExpenses(status, pageNumber, DEFAULT_PAGE_SIZE);
        }

        model.addAttribute("paging", new PagingHelper(page));
        model.addAttribute("page", page);

        return "expenses/list";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showExpense(Model model, @PathVariable Long id) {
        Expense expense = expenseService.getExpense(id);
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isViewableBy(currentUser)) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("statuses", Expense.Status.values());
        model.addAttribute("expense", expense);
        model.addAttribute("expenseRow", new ExpenseRow());
        model.addAttribute("receipts", receiptRepository.findByExpense(expense));

        if (expense.isEditableBy(currentUser)) {
            return "expenses/edit";
        } else {
            return "expenses/view";
        }
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newExpense(Model model) {
        model.addAttribute("expense", new Expense());
        return "expenses/new";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addExpense(@ModelAttribute Expense expense, BindingResult bindingResult,
            SessionStatus status, Model model) {

        User u = userService.getCurrentUser();
        expense.setUser(u);
        expense.setStatus(Expense.Status.DRAFT);
        // Set modified here to pass validation
        expense.setModified(new Date());

        expenseValidator.validate(expense, bindingResult);

        if (bindingResult.hasErrors()) {
            return "expenses/new";
        }

        expense = expenseService.saveExpense(expense);
        status.setComplete();

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String deleteExpense(@PathVariable Long id, SessionStatus status) {
        Expense expense = expenseService.getExpense(id);
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser)) {
            throw new ResourceNotFoundException();
        }

        expenseService.deleteExpense(expense);
        status.setComplete();

        return "redirect:/expenses";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateExpense(@PathVariable Long id, @Valid @ModelAttribute("expense") Expense expense,
            BindingResult bindingResult, SessionStatus status) {

        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser)) {
            throw new ResourceNotFoundException();
        }

        if (bindingResult.hasErrors()) {
            return "expenses/edit";
        }

        expense = expenseService.saveExpense(expense);
        status.setComplete();

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}/comments", method = RequestMethod.POST)
    public String addComment(@PathVariable Long id, @ModelAttribute Expense expense, SessionStatus status, String commentText) {
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isViewableBy(currentUser)) {
            throw new ResourceNotFoundException();
        }

        Comment comment = new Comment(expense, currentUser, commentText, new Date());
        commentRepository.save(comment);

        status.setComplete();

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}/send", method = RequestMethod.POST)
    public String sendForApproval(@PathVariable Long id, @ModelAttribute Expense expense, SessionStatus status) {
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();

        expense.setStatus(Expense.Status.SENT);
        expenseService.saveExpense(expense);

        status.setComplete();

        return "redirect:/expenses/";
    }

    @RequestMapping(value="/{id}/send", method = RequestMethod.GET)
    public String viewBeforeSend(Model model, @PathVariable Long id) {
        Expense expense = expenseService.getExpense(id);

        if (expense == null || !expense.isEditableBy(userService.getCurrentUser()))
            throw new ResourceNotFoundException();

        model.addAttribute("expense", expense);
        return "expenses/confirmSend";
    }
}
