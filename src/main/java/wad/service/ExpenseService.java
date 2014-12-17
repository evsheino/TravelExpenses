package wad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    public final static int FIRST_PAGE = 1;
    public final static int DEFAULT_PAGE_SIZE = 10;

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

    public Expense createExpense(Date startDate, Date endDate, String description, Expense.Status status) {
        return createExpense(userService.getCurrentUser(), startDate, endDate, description, status);
    }

    public Expense createExpense(User user, Date startDate, Date endDate, String description, Expense.Status status) {
        Expense e = new Expense();
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setModified(e.getStartDate());
        e.setUser(user);
        e.setDescription(description);
        e.setStatus(status );
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

    public Page<Expense> findAllExpensesPaged(Expense.Status status, Integer pageNumber, Integer perPage) {
        if(pageNumber == null) {
            pageNumber = 1;
        }

        if(perPage == null) {
            perPage = DEFAULT_PAGE_SIZE;
        }

        pageNumber = pageNumber-1;

        if(logger.isDebugEnabled()) {
            logger.debug("Finding expenses page "+ pageNumber +" ("+ perPage +" per page) by status "+ status);
        }

        return expenseRepository.findExpensesByStatusOrderByModifiedAsc(status, new PageRequest(pageNumber, perPage));
    }

    public Page<Expense> getPagedExpensesByUser(User user, Expense.Status status, Integer pageNumber, Integer perPage) {
        if(status == null) {
            status = Expense.Status.DRAFT;
        }

        if(pageNumber == null) {
            pageNumber = 1;
        }

        if(perPage == null) {
            perPage = 10;
        }

        pageNumber = pageNumber-1;

        Page<Expense> page = null;
        if(status == null) {
            if(logger.isDebugEnabled()) {
                logger.debug("Finding user "+ user.getUsername() +" expenses page "+ pageNumber +" ("+ perPage +" per page)");
            }
            page = expenseRepository.findAllByUserOrderByModifiedAsc(user, new PageRequest(pageNumber, perPage));
        } else {
            if(logger.isDebugEnabled()) {
                logger.debug("Finding user "+ user.getUsername() +" expenses page "+ pageNumber +" ("+ perPage +" per page) by status "+ status);
            }
            page = expenseRepository.findAllByUserAndStatusOrderByModifiedAsc(user, status, new PageRequest(pageNumber, perPage));
        }
        return page;
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findOne(id);
    }
}
