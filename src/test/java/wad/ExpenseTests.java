package wad;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import wad.repository.UserRepository;
import wad.service.UserService;

import org.springframework.test.web.servlet.MvcResult;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExpenseTests {
    private final String DATE_FORMAT = "dd/MM/yyyy";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    private User user;
    private Expense expense;

    @Before
    public void setUp() {
        assertEquals(0, expenseRepository.count());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
        this.webAppContext.getServletContext()
                .setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

        user = new User();

        user.setName("John Doe");
        user.setPassword("password");
        user.setUsername("johnd");

        userRepository.save(user);

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

    @Test
    public void expensePageWorks() throws Exception {
        expense = expenseRepository.save(expense);

        mockMvc.perform(get("/expenses/" + expense.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void expensePageModelHasExpense() throws Exception {
        expense = expenseRepository.save(expense);

        MvcResult res = mockMvc.perform(get("/expenses/" + expense.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("expense"))
                .andExpect(view().name("expenses/edit")).andReturn();

        assertNotNull(res.getModelAndView().getModel().get("expense"));

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        Expense resFromModel = (Expense) res.getModelAndView().getModel().get("expense");
        assertEquals(expense.getAmount(), resFromModel.getAmount());
        assertEquals(expense.getDescription(), resFromModel.getDescription());
        assertEquals(f.format(expense.getStartDate()), f.format(resFromModel.getStartDate()));
        assertEquals(f.format(expense.getEndDate()), f.format(resFromModel.getEndDate()));
        assertEquals(expense.getModified(), resFromModel.getModified());
        assertEquals(expense.getStatus(), resFromModel.getStatus());
        assertEquals(expense.getSupervisor(), resFromModel.getSupervisor());
        assertEquals(expense.getId(), resFromModel.getId());
    }

    /*
    TODO: Need to mock security context somehow so that current user is available.

    @Test
    public void postNewExpenseSavesExpense() throws Exception {
        mockMvc.perform(post("/expenses")
                .param("user", expense.getUser().getId().toString())
                .param("amount", expense.getAmount().toString())
                .param("status", expense.getStatus().toString())
                .param("startDate", expense.getStartDate().toString())
                .param("endDate", expense.getEndDate().toString())
                .param("description", expense.getDescription()));

        assertEquals(1, expenseRepository.count());

        Expense posted = expenseRepository.findAll().get(0);
        assertEquals(expense.getAmount(), posted.getAmount());
        assertEquals(expense.getDescription(), posted.getAmount());
        assertEquals(expense.getUser(), posted.getUser());
        assertEquals(expense.getStartDate(), posted.getStartDate());
        assertEquals(expense.getEndDate(), posted.getEndDate());
        assertEquals(expense.getStatus(), posted.getStatus());
    }
    */
}
