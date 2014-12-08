package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Expense createExpense(Date startDate, Date endDate, double amount, String description) {
        return createExpense(userService.getCurrentUser(), startDate, endDate, amount, description);
    }

    public Expense createExpense(User user, Date startDate, Date endDate, double amount, String description) {
        Expense e = new Expense();
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setModified(e.getStartDate());
        e.setUser(user);
        e.setDescription(description);
        e.setStatus(Expense.Status.DRAFT);
        return expenseRepository.save(e);
    }

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    public Page<Expense> getPagedExpenses(Integer pageNumber, Integer perPage) {
        return getPagedExpensesByUser(userService.getCurrentUser(), null, pageNumber, perPage);
    }

    public Page<Expense> getPagedExpenses(Expense.Status status, Integer pageNumber, Integer perPage) {
        return getPagedExpensesByUser(userService.getCurrentUser(), status, pageNumber, perPage);
    }

    public Page<Expense> getPagedExpensesByUser(User user, Expense.Status status, Integer pageNumber, Integer perPage) {
        if(status == null) {
            status = Expense.Status.DRAFT;
        }

        if(pageNumber == null) {
            pageNumber = 0;
        }

        if(perPage == null) {
            perPage = 10;
        }

        Page<Expense> page = null;
        if(status == null) {
            page = expenseRepository.findAllByUser(user, new PageRequest(pageNumber, perPage));
        } else {
            page = expenseRepository.findAllByUserAndStatus(user, status, new PageRequest(pageNumber, perPage));
        }
        return page;
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findOne(id);
    }
}
