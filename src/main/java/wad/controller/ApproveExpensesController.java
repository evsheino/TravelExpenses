package wad.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wad.domain.Comment;
import wad.domain.Expense;
import wad.repository.CommentRepository;
import wad.repository.ExpenseRepository;
import wad.service.ExpenseService;
import wad.service.UserService;
import wad.util.PagingHelper;

@Controller
@RequestMapping("/expenses/approve")
@Secured("ROLE_SUPERVISOR")
public class ApproveExpensesController {

    private static final Logger logger = LoggerFactory.getLogger(ApproveExpensesController.class);

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public String listExpenses(Model model, @RequestParam(required = false) Integer pageNumber) {
        Page<Expense> page = expenseService.findAllExpensesPaged(Expense.Status.SENT, pageNumber, 10);

        if(logger.isDebugEnabled()) {
            logger.debug("Got "+ page.getTotalPages() +" pages of total "+ page.getTotalElements());
        }

        model.addAttribute("approve", Boolean.TRUE);
        model.addAttribute("status", Expense.Status.SENT.toString());
        model.addAttribute("paging", new PagingHelper(page));
        model.addAttribute("page", page);

        return "expenses/approveList";
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String viewExpense(Model model, @PathVariable Long id) {
        model.addAttribute("expense", expenseService.getExpense(id));
        return "expenses/approve";
    }

    @RequestMapping(value="/{id}/approve", method = RequestMethod.POST)
    public String approveExpense(Model model, @PathVariable Long id) {
        updateStatus(id, Expense.Status.APPROVED);
        return "redirect:/expenses/approve/list";
    }

    @RequestMapping(value="/{id}/reject", method = RequestMethod.POST)
    @Transactional
    public String rejectExpense(Model model, @PathVariable Long id, String commentText) {
        Expense expense = expenseService.getExpense(id);

        Comment comment = new Comment(expense, userService.getCurrentUser(), commentText, new Date());
        commentRepository.save(comment);
        //expense.getComments().add(comment);

        updateStatus(expense, Expense.Status.REJECTED);
        return "redirect:/expenses/approve/list";
    }

    private void updateStatus(Long id, Expense.Status status) {
        Expense expense = expenseService.getExpense(id);
        updateStatus(expense, status);
    }

    private void updateStatus(Expense expense, Expense.Status status) {
        expense.setStatus(status);
        expenseService.saveExpense(expense);
    }

}
