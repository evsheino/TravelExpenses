package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wad.domain.Expense;
import wad.repository.ExpenseRepository;

@Controller
@RequestMapping("/expenses/approve")
public class ApproveExpensesController {

    @Autowired
    private ExpenseRepository expenseRepository;

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
    public String rejectExpense(Model model, @PathVariable Long id) {
        updateStatus(id, Expense.Status.REJECTED);
        return "redirect:/expenses/approve/list";
    }

    private void updateStatus(Long id, Expense.Status status) {
        Expense expense = expenseRepository.findOne(id);
        expense.setStatus(status);
        expenseRepository.save(expense);
    }

}
