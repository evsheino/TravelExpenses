package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import wad.service.ExpenseService;

@Controller
@RequestMapping("/expenses")
@SessionAttributes("expense")
public class ExpensesController {
    
    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
        dataBinder.setDisallowedFields("user");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @ModelAttribute("expense")
    private Expense getExpense() {
        return new Expense();
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
        if (expenseService.checkStartAndEndDate(expense.getStartDate(), expense.getEndDate()) == false) {
            model.addAttribute("error", "Chosen starting date must always be same or earlier than ending date.");
            return "expenses/new";
        }
        User u = userService.getCurrentUser();
        expense.setUser(u);
        expense.setStatus(Expense.Status.SAVED);

        expense = expenseService.saveExpense(expense);
        status.setComplete();

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
    public String updateExpense (@PathVariable Long id, @ModelAttribute Expense expense, 
            SessionStatus status, BindingResult bindingResult) {
        User currentUser = userService.getCurrentUser();

        if (expense == null || !expense.isEditableBy(currentUser))
            throw new ResourceNotFoundException();

        expense = expenseService.saveExpense(expense);
        status.setComplete();

        return "redirect:/expenses/" + expense.getId();
    }
}
