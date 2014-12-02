package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import java.util.Date;
import java.util.List;
import wad.domain.Authority;
import wad.domain.ExpenseRow;

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

    public Expense createExpense(Date startDate, Date endDate, double amount, String description) {
        return createExpense(userService.getCurrentUser(), startDate, endDate, amount, description);
    }

    public Expense createExpense(User user, Date startDate, Date endDate, double amount, String description) {
        Expense e = new Expense();
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setModified(e.getStartDate());
        e.setUser(user);
        e.setAmount(amount);
        e.setDescription(description);
        e.setStatus(Expense.Status.SAVED);
        return expenseRepository.save(e);
    }

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findOne(id);
    }
    
    public void updateExpenseAmount (Expense expense) {
        Double newSum;
        double helpSum = 0;
        
        List<ExpenseRow> expenseRows = expense.getExpenseRows();
        
        for (ExpenseRow expenseRow : expenseRows) {
            helpSum += expenseRow.getAmount();
        }
        newSum = (Double) helpSum;
        expense.setAmount(newSum);
        expenseRepository.save(expense);
    }
}
