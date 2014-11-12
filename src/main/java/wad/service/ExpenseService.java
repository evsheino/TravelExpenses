package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import java.util.Date;
import java.util.List;

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
    
    public void deleteExpense(Expense exp) {
       expenseRepository.delete(exp);
    }

    public Expense createExpense(Date date, double amount, String description) {
        return createExpense(userService.getCurrentUser(), date, amount, description);
    }

    public Expense createExpense(User user, Date date, double amount, String description) {
        Expense e = new Expense();
        e.setDate(date);
        e.setModified(e.getDate());
        e.setUser(user);
        e.setAmount(amount);
        e.setDescription(description);
        return expenseRepository.save(e);
    }

    public Expense updateExpense(Expense e) {
        // Update expense report on background
        return expenseRepository.save(e);
    }

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findOne(id);
    }


}
