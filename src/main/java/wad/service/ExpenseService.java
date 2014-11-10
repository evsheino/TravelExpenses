package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import wad.domain.Authority;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nryytty@cs on 10.11.2014.
 */
@Service
public class ExpenseService {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    public Expense saveExpense(Expense exp) {
        exp.setModified(new Date());
        return expenseRepository.save(exp);
    }

    public Expense createExpense(Date date, double amount) {
        return createExpense(userService.getCurrentUser(), date, amount);
    }

    public Expense createExpense(User user, Date date, double amount) {
        Expense e = new Expense();
        e.setDate(date);
        e.setModified(e.getDate());
        e.setUser(user);
        e.setAmount(amount);
        return expenseRepository.save(e);
    }

    public Expense updateExpense(Expense e) {
        // Update expense report on background
        return expenseRepository.save(e);
    }


}
