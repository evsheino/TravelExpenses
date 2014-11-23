package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import wad.domain.Expense;
import wad.domain.User;
import wad.service.ExpenseService;

@Controller
@RequestMapping("/expenses")
public class ExpensesController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

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
        return "expenses/edit";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addExpense (@ModelAttribute Expense expense, BindingResult bindingResult, ModelMap model) {
        // Error handling.
        if (expenseService.checkStartAndEndDate(expense.getStartDate(), expense.getEndDate()) == false) {
            model.addAttribute("error", "Chosen starting date must always be same or earlier than ending date.");
            return "index";
        }
        
        
        User u = userService.getCurrentUser();
        expense.setUser(u);
        expense.setStatus(Expense.Status.SAVED);

        expense = expenseService.saveExpense(expense);

        return "redirect:/expenses/" + expense.getId();
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String deleteExpense (@PathVariable Long id) {
        Expense expense = expenseService.getExpense(id);
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();

        expenseService.deleteExpense(expense);

        return "redirect:/expenses";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateExpense (@PathVariable Long id, @ModelAttribute Expense updated, BindingResult bindingResult) {
        User currentUser = userService.getCurrentUser();
        Expense expense = expenseService.getExpense(id);

        if (expense == null || 
                !(expense.isEditableBy(currentUser) && updated.isEditableBy(currentUser)))
            throw new ResourceNotFoundException();

        expense = expenseService.updateExpense(expense, updated);

        return "redirect:/expenses/" + expense.getId();
    }
}
