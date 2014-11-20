package wad;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;

import wad.repository.UserRepository;
import wad.service.UserService;

import org.springframework.test.web.servlet.MvcResult;
import wad.controller.ResourceNotFoundException;
import wad.domain.Authority;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExpenseControllerTests {
    private final String DATE_FORMAT = "dd/MM/yyyy";
    private final String NAME = "John Doe";
    private final String USERNAME = "user";
    private final String PASSWORD = "password";

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

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
    private User user2;
    private Expense expense;
    private MockHttpSession session;

    @Before
    public void setUp() throws Exception {
        assertEquals(0, expenseRepository.count());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilterChain).build();
        this.webAppContext.getServletContext()
                .setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

        user = userService.createUser(NAME, USERNAME, PASSWORD, Authority.Role.USER);
        user2 = userService.createUser("Ludwig Wittgenstein", "ludwig", "tractatus", Authority.Role.USER);

        userRepository.save(user);

        expense = new Expense();
        expense.setUser(user);
        expense.setStartDate(new Date());
        expense.setEndDate(new Date());
        expense.setDescription("blaa blaa");
        expense.setStatus(Expense.Status.SAVED);
        expense.setModified(new Date());
        expense.setAmount(100.0);

        session = (MockHttpSession) mockMvc.perform(formLogin("/authenticate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn()
                .getRequest()
                .getSession();
        assertNotNull(session); 
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        expenseRepository.deleteAll();
    }

    @Test
    public void expensePageWorks() throws Exception {
        expense = expenseRepository.save(expense);

        mockMvc.perform(get("/expenses/" + expense.getId()).session(session))
                .andExpect(status().isOk());
    }

    @Test
    public void expensePageModelHasExpense() throws Exception {
        expense = expenseRepository.save(expense);

        MvcResult res = mockMvc.perform(get("/expenses/" + expense.getId()).with(user(USERNAME)))
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

    @Test
    public void postUpdatedExpenseUpdatesExpense() throws Exception {
        expense = expenseRepository.save(expense);

        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";
        String amount = "200";
        Expense.Status status = Expense.Status.SAVED;

        String url = "/expenses/" + expense.getId();
        mockMvc.perform(post(url).session(session).with(csrf())
                .param("user", user.getId().toString())
                .param("amount", amount)
                .param("status", status.toString())
                .param("startDate", startDate)
                .param("endDate", endDate)
                .param("description", desc))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(url));

        assertEquals("There should be exactly 1 Expense in the database after updating the only existing Expense.",
                1, expenseRepository.count());

        // Check that the Expense was updated.
        Expense posted = expenseRepository.findAll().get(0);
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        assertEquals(f.parse(startDate), posted.getStartDate());
        assertEquals(f.parse(endDate), posted.getEndDate());
        assertEquals(desc, posted.getDescription());
        assertEquals(200, posted.getAmount(), 0.001);
        assertEquals(user, posted.getUser());
        assertEquals(status, posted.getStatus());
    }

    @Test
    public void updateAnotherUsersExpenseFails() throws Exception {
        expense = expenseRepository.save(expense);

        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";
        String amount = "200";
        Expense.Status status = Expense.Status.APPROVED;

        String url = "/expenses/" + expense.getId();
        mockMvc.perform(post(url).session(session).with(csrf())
                .param("user", user2.getId().toString())
                .param("amount", amount)
                .param("status", status.toString())
                .param("startDate", startDate)
                .param("endDate", endDate)
                .param("description", desc))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void postNewExpenseCreatesNewExpense() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        String url = "/expenses/";
        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("user", expense.getUser().getId().toString())
                .param("amount", expense.getAmount().toString())
                .param("status", expense.getStatus().toString())
                .param("startDate", f.format(expense.getStartDate()))
                .param("endDate", f.format(expense.getEndDate()))
                .param("description", expense.getDescription()))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertEquals("There should be exactly 1 Expense in the database after creating the first Expense.",
                1, expenseRepository.count());

        Expense posted = expenseRepository.findAll().get(0);

        assertEquals("redirect:" + url + posted.getId(), res.getModelAndView().getViewName());

        assertEquals(expense.getAmount(), posted.getAmount());
        assertEquals(expense.getDescription(), posted.getDescription());
        assertEquals(expense.getUser(), posted.getUser());
        assertEquals(f.format(expense.getStartDate()), f.format(posted.getStartDate()));
        assertEquals(f.format(expense.getEndDate()), f.format(posted.getEndDate()));
        assertEquals(expense.getStatus(), posted.getStatus());
    }

    @Test
    public void deleteExpenseByOwnerDeletesExpenseWithStatusSAVED() throws Exception {
        expense.setStatus(Expense.Status.SAVED);
        expense = expenseRepository.save(expense);
        assertEquals(1, expenseRepository.count());

        String url = "/expenses/" + expense.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()));

        assertEquals("After deleting the Expense only Expense, there should be no Expenses in the database.",
                0, expenseRepository.count());

    }

    @Test
    public void deleteExpenseDeletesCorrectExpense() throws Exception {
        String desc = "do not delete this";

        expense.setStatus(Expense.Status.SAVED);
        expense.setDescription(desc);
        expenseRepository.save(expense);

        expense = new Expense();
        expense.setUser(user);
        expense.setStartDate(new Date());
        expense.setEndDate(new Date());
        expense.setDescription("DELETE THIS");
        expense.setStatus(Expense.Status.SAVED);
        expense.setModified(new Date());
        expense.setAmount(100.0);
        expense = expenseRepository.save(expense);

        assertEquals(2, expenseRepository.count());

        Long id = expense.getId();

        String url = "/expenses/" + id + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));

        assertEquals("After deleting an Expense, there should be one less Expenses in the database.",
                1, expenseRepository.count());
        assertNull("After deleting an Expense, the deleted Expense should not be in the database.",
                expenseRepository.findOne(id));

        String actualDesc = expenseRepository.findAll().get(0).getDescription();

        assertEquals("After deleting the other Expense, the one left in the database should have the description '"
                + desc + "', but instead had '" + actualDesc + "'" , desc, actualDesc);

    }

    private void testDeleteFails(Expense expense) throws Exception {
        long count = expenseRepository.count();

        String url = "/expenses/" + expense.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()));
        assertEquals(count, expenseRepository.count());
    }

    @Test
    public void deleteExpenseByNonOwnerNonAdminFails() throws Exception {
        expense.setUser(user2);
        expense = expenseRepository.save(expense);
        testDeleteFails(expense);
    }

    @Test
    public void deleteExpenseByOwnerWithStatusAPPROVEDFails() throws Exception {
        expense.setStatus(Expense.Status.APPROVED);
        expense = expenseRepository.save(expense);
        testDeleteFails(expense);
    }

    @Test
    public void deleteExpenseByOwnerWithStatusWAITINGFails() throws Exception {
        expense.setStatus(Expense.Status.WAITING);
        expense = expenseRepository.save(expense);
        testDeleteFails(expense);
    }
}
