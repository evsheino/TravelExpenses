package wad.controller;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wad.domain.Comment;
import wad.domain.Expense;
import wad.repository.CommentRepository;
import wad.repository.ExpenseRepository;
import wad.service.UserService;

@Controller
@RequestMapping("/expenses/approve")
public class ApproveExpensesController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public String listExpenses(Model model) {
        model.addAttribute("expenses", expenseRepository.findExpensesByStatusOrderByModifiedAsc(Expense.Status.DRAFT));
        return "expenses/approveList";
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String viewExpense(Model model, @PathVariable Long id) {
        model.addAttribute("expense", expenseRepository.findOne(id));
        return "expenses/approve";
    }

    @RequestMapping(value="/{id}/approve", method = RequestMethod.POST)
    public String approveExpense(Model model, @PathVariable Long id) {
        updateStatus(id, Expense.Status.APPROVED);
        return "redirect:/expenses/approve/list";
    }

    @RequestMapping(value="/{id}/reject", method = RequestMethod.POST)
    public String rejectExpense(Model model, @PathVariable Long id, String commentText) {
        Expense expense = expenseRepository.findOne(id);

        Comment comment = new Comment(expense, userService.getCurrentUser(), commentText, new Date());
        commentRepository.save(comment);
        expense.getComments().add(comment);

        updateStatus(expense, Expense.Status.REJECTED);
        return "redirect:/expenses/approve/list";
    }

    private void updateStatus(Long id, Expense.Status status) {
        Expense expense = expenseRepository.findOne(id);
        updateStatus(expense, status);
    }

    private void updateStatus(Expense expense, Expense.Status status) {
        expense.setStatus(status);
        expenseRepository.save(expense);
    }

}
