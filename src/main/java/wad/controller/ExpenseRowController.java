package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRowRepository;
import wad.service.ExpenseService;

@Controller
@RequestMapping("/expenses/{expenseId}/rows")
public class ExpenseRowController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRowRepository expenseRowRepository;

    @RequestMapping(method = RequestMethod.POST)
    public String addExpenseRow (@PathVariable Long expenseId, @ModelAttribute ExpenseRow row,
            BindingResult bindingResult) {

        User user = userService.getCurrentUser();
        Expense expense = expenseService.getExpense(expenseId);

        if (expense == null || !expense.isEditableBy(user))
            throw new ResourceNotFoundException();

        row.setExpense(expense);
        expenseRowRepository.save(row);

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String deleteExpenseRow (@PathVariable Long expenseId, @PathVariable Long id) {
        Expense expense = expenseService.getExpense(expenseId);
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();

        expenseRowRepository.delete(id);

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateExpenseRow (@PathVariable Long expenseId, @PathVariable Long id,
            @ModelAttribute ExpenseRow row, BindingResult bindingResult) {

        User user = userService.getCurrentUser();
        Expense expense = row.getExpense();

        if (expense == null || !expense.isEditableBy(user))
            throw new ResourceNotFoundException();

        expenseRowRepository.save(row);

        return "redirect:/expenses/" + expense.getId();
    }
}
