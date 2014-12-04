package wad.controller;

import java.util.Date;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRowRepository;
import wad.service.ExpenseService;
import wad.validator.ExpenseValidator;

@Controller
@RequestMapping("/expenses")
@SessionAttributes("expense")
public class ExpensesController {
    
    @Autowired
    @Qualifier("expenseValidator")
    ExpenseValidator expenseValidator;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRowRepository expenseRowRepository;

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
    public String listExpenses(Model model) {
        model.addAttribute("expenses", expenseService.getExpensesByUser(userService.getCurrentUser()));
        return "expenses/list";
    }
    
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String showExpense(Model model, @PathVariable Long id) {
        Expense expense = expenseService.getExpense(id);
        
        if (expense == null || !expense.isViewableBy(userService.getCurrentUser()))
            throw new ResourceNotFoundException();
        
        model.addAttribute("statuses", Expense.Status.values());
        model.addAttribute("expense", expense);
        model.addAttribute("expenseRow", new ExpenseRow());
        return "expenses/edit";
    }
    
    @RequestMapping(value="/new", method = RequestMethod.GET)
    public String newExpense(Model model) {
        model.addAttribute("expense", new Expense());
        return "expenses/new";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String addExpense(@ModelAttribute Expense expense, BindingResult bindingResult,
            SessionStatus status, Model model) {

        User u = userService.getCurrentUser();
        expense.setUser(u);
        expense.setStatus(Expense.Status.SAVED);
        // Set modified here to pass validation
        expense.setModified(new Date());

        expenseValidator.validate(expense, bindingResult);

        if (bindingResult.hasErrors())
            return "expenses/new";

        expense = expenseService.saveExpense(expense);
        status.setComplete();

        return "redirect:/expenses/" + expense.getId();
    }
    
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String deleteExpense (@PathVariable Long id, SessionStatus status) {
        Expense expense = expenseService.getExpense(id);
        User currentUser = userService.getCurrentUser();
        
        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();
        
        expenseService.deleteExpense(expense);
        status.setComplete();
        
        return "redirect:/expenses";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateExpense (@PathVariable Long id, @Valid @ModelAttribute("expense") Expense expense, 
            BindingResult bindingResult, SessionStatus status) {

        if (bindingResult.hasErrors())
            return "expenses/edit";
        
        User currentUser = userService.getCurrentUser();
        
        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();

        if (bindingResult.hasErrors())
            return "expenses/edit";

        expense = expenseService.saveExpense(expense);
        status.setComplete();
        
        return "redirect:/expenses/" + expense.getId();
    }
}
