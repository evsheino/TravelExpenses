package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRowRepository;
import wad.service.ExpenseService;
import wad.validator.ExpenseRowValidator;

@Controller
@SessionAttributes("expense")
@RequestMapping("/expenses/{expenseId}/rows")
public class ExpenseRowController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRowRepository expenseRowRepository;

    @Autowired
    private ExpenseRowValidator validator;

    @ModelAttribute("expenseRow")
    private ExpenseRow getExpenseRow() {
        return new ExpenseRow();
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addExpenseRow (@PathVariable Long expenseId, @ModelAttribute ExpenseRow expenseRow,
            BindingResult bindingResult, RedirectAttributes attrs) {

        User user = userService.getCurrentUser();
        Expense expense = expenseService.getExpense(expenseId);

        if (expense == null || !expense.isEditableBy(user))
            throw new ResourceNotFoundException();

        expenseRow.setExpense(expense);

        validator.validate(expenseRow, bindingResult);

        if (bindingResult.hasErrors()) {
            return "expenses/edit";
        }

        expenseRowRepository.save(expenseRow);

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String deleteExpenseRow (@PathVariable Long expenseId, @PathVariable Long id) {
        ExpenseRow row = expenseRowRepository.findOne(id);
        Expense expense = row.getExpense();
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();

        expense.getExpenseRows().remove(row);
        expenseRowRepository.delete(row);

        return "redirect:/expenses/" + expense.getId();
    }
}
