package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.UserRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/expences")
public class ExpensesController {

    @Autowired
    private UserRepository userRepository;

    private List<Expense> generateTempData() {
        List<Expense> expenses = new ArrayList<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        for(int i = 0; i < 10; i++) {
            Expense e = new Expense();
            e.setDate(new Date());
            e.setUser(user);
            e.setAmount((i^2) + 11.99 + 1);
        }
        return expenses;
    }

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public List<Expense> listExpenses() {
        return generateTempData();
    }

}
