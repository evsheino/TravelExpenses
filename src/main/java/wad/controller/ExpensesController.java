package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;
import wad.service.UserService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/expences")
public class ExpensesController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public List<Expense> listExpenses() {
        User user = userService.getCurrentUser();
        return expenseRepository.findByUser(user);
    }

}
