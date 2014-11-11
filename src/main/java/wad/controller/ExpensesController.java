package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.service.UserService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/expenses")
public class ExpensesController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public List<Expense> listExpenses() {
        User user = userService.getCurrentUser();
        user.getExpenses().size();
        return user.getExpenses();
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String showExpense(Model model, @PathVariable Long id) {
        model.addAttribute("expense", expenseRepository.findOne(id));
        return "expenses";
    }

}
