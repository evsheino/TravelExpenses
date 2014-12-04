package wad.domain;

import java.util.ArrayList;
import wad.*;
import java.util.Date;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExpenseTests {
    
    private Expense expense;
    private User user;
    private User user2;
    private User supervisor;
    private User admin;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Before
    public void setUp() {
        Authority adminAuth = new Authority();
        adminAuth.setRole(Authority.Role.ADMIN);
        Authority supervisorAuth = new Authority();
        supervisorAuth.setRole(Authority.Role.SUPERVISOR);
        Authority userAuth = new Authority();
        userAuth.setRole(Authority.Role.USER);
        
        user = new User();
        user.setName("User");
        user.setPassword("password");
        user.setUsername("johnd");
        user.setAuthorities(new ArrayList());
        user.getAuthorities().add(userAuth);
        user = userRepository.save(user);

        user2 = new User();
        user2.setName("User2");
        user2.setPassword("password");
        user2.setUsername("johnd2");
        user2.setAuthorities(new ArrayList());
        userAuth = new Authority();
        userAuth.setRole(Authority.Role.USER);
        user2.getAuthorities().add(userAuth);
        user2 = userRepository.save(user2);
        
        admin = new User();
        admin.setName("Admin");
        admin.setPassword("password");
        admin.setUsername("admin");
        admin.setAuthorities(new ArrayList());
        admin.getAuthorities().add(adminAuth);
        admin = userRepository.save(admin);

        supervisor = new User();
        supervisor.setName("Supervisor");
        supervisor.setPassword("password");
        supervisor.setUsername("supervisor");
        supervisor.setAuthorities(new ArrayList());
        supervisor.getAuthorities().add(supervisorAuth);
        supervisor = userRepository.save(supervisor);
        
        expense = new Expense();
        expense.setUser(user);
        expense.setStartDate(new Date());
        expense.setEndDate(new Date());
        expense.setDescription("blaa blaa");
        expense.setStatus(Expense.Status.SAVED);
        expense.setModified(new Date());
        expense = expenseRepository.save(expense);
    }

    @After
    public void cleanup() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();
    }

    
    @Test
    public void isEditableByReturnsTrueForAdmin() throws Exception {
        expense.setStatus(Expense.Status.SAVED);
        assertTrue(expense.isEditableBy(admin));

        expense.setStatus(Expense.Status.RETURNED);
        assertTrue(expense.isEditableBy(admin));

        expense.setStatus(Expense.Status.WAITING);
        assertTrue(expense.isEditableBy(admin));

        expense.setStatus(Expense.Status.APPROVED);
        assertTrue(expense.isEditableBy(admin));
    }

    @Test
    public void isEditableByReturnsTrueForOwnerWhenStatusIsSAVED() throws Exception {
        expense.setStatus(Expense.Status.SAVED);
        assertTrue(expense.isEditableBy(user));
    }

    @Test
    public void isEditableByReturnsTrueForOwnerWhenStatusIsRETURNED() throws Exception {
        expense.setStatus(Expense.Status.RETURNED);
        assertTrue(expense.isEditableBy(user));
    }

    @Test
    public void isEditableByReturnsFalseForOwnerWhenStatusIsAPPROVED() throws Exception {
        expense.setStatus(Expense.Status.APPROVED);
        assertFalse(expense.isEditableBy(user));
    }
    
    @Test
    public void isEditableByReturnsFalseForOwnerWhenStatusIsWAITING() throws Exception {
        expense.setStatus(Expense.Status.WAITING);
        assertFalse(expense.isEditableBy(user));
    }

    @Test
    public void isEditableByReturnsFalseForNonOwnerUser() throws Exception {
        expense.setStatus(Expense.Status.SAVED);
        assertFalse(expense.isEditableBy(user2));

        expense.setStatus(Expense.Status.RETURNED);
        assertFalse(expense.isEditableBy(user2));

        expense.setStatus(Expense.Status.WAITING);
        assertFalse(expense.isEditableBy(user2));

        expense.setStatus(Expense.Status.APPROVED);
        assertFalse(expense.isEditableBy(user2));
    }

    @Test
    public void isEditableByReturnsFalseForNonOwnerSupervisor() throws Exception {
        expense.setStatus(Expense.Status.SAVED);
        assertFalse(expense.isEditableBy(supervisor));

        expense.setStatus(Expense.Status.RETURNED);
        assertFalse(expense.isEditableBy(supervisor));

        expense.setStatus(Expense.Status.WAITING);
        assertFalse(expense.isEditableBy(supervisor));

        expense.setStatus(Expense.Status.APPROVED);
        assertFalse(expense.isEditableBy(supervisor));
    }
}
