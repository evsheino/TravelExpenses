package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
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
        model.addAttribute("statuses", Expense.Status.values());
        model.addAttribute("expense", expenseService.getExpense(id));
        return "expenses/edit";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String addExpense (@ModelAttribute Expense expense, BindingResult bindingResult) {

        User u = userService.getCurrentUser();
        expense.setUser(u);

        expenseService.saveExpense(expense);
        
        return "redirect:/expenses/" + expense.getId();
    }
    
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String deleteExpense (@PathVariable Long id) {
        Expense e = expenseService.getExpense(id);
        expenseService.deleteExpense(e);
        
        return "redirect:/expenses";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateExpense (@PathVariable Long id, @ModelAttribute Expense updated, BindingResult bindingResult) {
        User currentUser = userService.getCurrentUser();
        Expense expense = expenseService.getExpense(id);

        if (expense == null)
            throw new ResourceNotFoundException();

        // Only allow admins to edit any Expense - others can only edit 
        // their own Expenses.
        if (!currentUser.isAdmin()
                && (currentUser != expense.getUser() || currentUser != updated.getUser())) {
            throw new ResourceNotFoundException();
        }
        expense = expenseService.updateExpense(expense, updated);
        
        return "redirect:/expenses/" + expense.getId();
    }    
}
