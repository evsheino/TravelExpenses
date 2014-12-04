package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import wad.domain.Authority;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.ExpenseRowRepository;
import wad.service.ExpenseService;
import wad.service.UserService;
import wad.validator.ExpenseValidator;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.Date;

@Controller
@RequestMapping("/expenses/approve")
public class ApproveExpensesController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public String listExpenses(Model model) {
        model.addAttribute("expenses", expenseRepository.findExpensesByStatusOrderByModifiedAsc(Expense.Status.SAVED));
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
        updateStatus(id, Expense.Status.RETURNED);
        return "redirect:/expenses/approve/list";
    }

    private void updateStatus(Long id, Expense.Status status) {
        Expense expense = expenseRepository.findOne(id);
        expense.setStatus(status);
        expenseRepository.save(expense);
    }

}
