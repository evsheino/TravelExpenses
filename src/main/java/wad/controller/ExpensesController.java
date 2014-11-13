package wad.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
        model.addAttribute("expense", expenseService.getExpense(id));
        return "expenses/edit";
    }
    
    // Ehkä kyseessä on hunosti määritelty polku?
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addExpense (@Valid @ModelAttribute Expense e) {
        User u = userService.getCurrentUser();
        e.setUser(u);
        expenseService.saveExpense(e);
        
        return "redirect: expenses";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteExpense (@PathVariable Long id) {
        Expense e = expenseService.getExpense(id);
        expenseService.deleteExpense(e);
        
        return "redirect: expenses";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String changeExpense (@Valid @ModelAttribute Expense expense) {
        expenseService.saveExpense(expense);
        
        return "redirect: expenses/edit";
    }    
}
