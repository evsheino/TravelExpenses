package wad;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import wad.domain.Expense;
import wad.repository.AuthorityRepository;
import wad.repository.UserRepository;
import wad.service.UserService;

import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.service.ExpenseService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExpenseServiceTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    private Expense expense;
    private User user;
    private User user2;

    @Before
    public void setUp() {
        assertEquals(0, expenseRepository.count());

        user = new User();

        user.setName("John Doe");
        user.setPassword("password");
        user.setUsername("johnd");

        userRepository.save(user);

        user2 = new User();
        user2.setName("Immanuel Kant");
        user2.setPassword("password");
        user2.setUsername("kant");
        userRepository.save(user2);

        expense = new Expense();
        expense.setUser(user);
        expense.setStartDate(new Date());
        expense.setEndDate(new Date());
        expense.setDescription("blaa blaa");
        expense.setStatus(Expense.Status.SAVED);
        expense.setModified(new Date());
        expense.setAmount(100.0);
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        expenseRepository.deleteAll();
    }

    // TODO: Add test for createExpense(Date startDate, Date endDate, double amount, String description)

    @Test
    public void saveExpenseCreatesANewExpenseWhenDoesNotExist() throws Exception {
        expense = expenseService.saveExpense(expense);

        assertEquals(1, expenseRepository.count());

        Expense fetched = expenseRepository.findOne(expense.getId());
        assertEquals(fetched, expense);
    }

    @Test
    public void saveExpenseModifiesModified() throws Exception {
        expense.setModified(new Date(System.currentTimeMillis() - 3600 * 1000));

        Date old = expense.getModified();
        expense = expenseService.saveExpense(expense);

        assertTrue(expense.getModified().after(old));
    }

    @Test
    public void saveExpenseCreatesANewExpenseWhenExpenseDoesNotExist() throws Exception {
        expense = expenseService.saveExpense(expense);

        assertEquals(1, expenseRepository.count());

        Expense fetched = expenseRepository.findOne(expense.getId());
        assertEquals(fetched, expense);
    }

    @Test
    public void updateExpenseUpdatesExpense() throws Exception {
        Expense old = expenseRepository.save(expense);

        assertEquals(1, expenseRepository.count());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date d = sdf.parse("21/12/2012");

        expense.setAmount(22.0);
        expense.setDescription("blah");
        expense.setStartDate(d);
        expense.setEndDate(d);
        expense.setStatus(Expense.Status.APPROVED);
        expense.setSupervisor(user2);

        expense = expenseService.updateExpense(old, expense);

        assertEquals(1, expenseRepository.count());

        Expense updated = expenseRepository.findOne(expense.getId());

        assertEquals(updated, expense);
    }

    @Test
    public void updateExpenseReturnsNullIfExpenseNotFound() throws Exception {
        assertNull(expenseService.updateExpense(new Expense(), expense));
        assertEquals(0, expenseRepository.count());
    }

    @Test
    public void getExpensesByUserReturnsUsersExpenses() throws Exception {
        // Add one expense to another user as to
        // check that only the requested user's expenses are returned.

        expense.setUser(user2);
        expenseRepository.save(expense);


        List<Expense> list = new ArrayList();

        for (int i=0; i < 6; i++) {
            Expense e = new Expense();
            e.setUser(user);
            e.setStartDate(new Date());
            e.setEndDate(new Date());
            e.setDescription("blaa blaa" + i);
            e.setStatus(Expense.Status.SAVED);
            e.setModified(new Date());

            list.add(e);
        }
        list = expenseRepository.save(list);

        assertEquals(7, expenseRepository.count());

        List<Expense> received = expenseService.getExpensesByUser(user);

        assertEquals(list, received);
    }

    @Test
    public void getExpensesByUserReturnsEmptyListIfUserHasNoExpenses() throws Exception {
        assertTrue(expenseService.getExpensesByUser(user).isEmpty());
    }

    @Test
    public void getExpenseReturnsCorrectExpense() throws Exception {
        expense = expenseRepository.save(expense);
        assertEquals(expense, expenseService.getExpense(expense.getId()));
    }
    
    @Test
    public void getExpenseReturnsNullIfExpenseNotFound() throws Exception {
        assertNull(expenseService.getExpense(1L));
    }
}
