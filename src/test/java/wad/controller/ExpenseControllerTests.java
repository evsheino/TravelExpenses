package wad.controller;

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
import wad.Application;
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

    private final String NAME2 = "David Hume";
    private final String USERNAME2 = "hume";
    private final String PASSWORD2 = "treatise";

    private final String ADMIN_NAME = "Immanuel Kant";
    private final String ADMIN_USERNAME = "kant";
    private final String ADMIN_PASSWORD = "dingansich";

    private final String SUPERVISOR_NAME = "Ludwig Wittgenstein";
    private final String SUPERVISOR_USERNAME = "ludwig";
    private final String SUPERVISOR_PASSWORD = "tractatus";

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
    private User admin;
    private User supervisor;

    // Persisted
    private Expense expense;
    // Not persisted
    private Expense unsavedExpense;

    private long initialExpenseCount;

    private MockHttpSession session;

    @Before
    public void setUp() throws Exception {
        assertEquals(0, expenseRepository.count());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilterChain).build();
        this.webAppContext.getServletContext()
                .setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

        user = userService.createUser(NAME, USERNAME, PASSWORD, Authority.Role.USER);
        user2 = userService.createUser(NAME2, USERNAME2, PASSWORD2, Authority.Role.USER);
        admin = userService.createUser(ADMIN_NAME, ADMIN_USERNAME, ADMIN_PASSWORD, Authority.Role.ADMIN);
        supervisor = userService.createUser(SUPERVISOR_NAME, SUPERVISOR_USERNAME, SUPERVISOR_PASSWORD, Authority.Role.SUPERVISOR);

        userRepository.save(user);

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        expense = new Expense();
        expense.setUser(user);
        expense.setStartDate(f.parse("20/09/2014"));
        expense.setEndDate(f.parse("29/09/2014"));
        expense.setDescription("blaa blaa");
        expense.setStatus(Expense.Status.SAVED);
        expense.setModified(new Date());
        expense.setAmount(100.9);
        expense = expenseRepository.save(expense);

        unsavedExpense = new Expense();
        unsavedExpense.setUser(user);
        unsavedExpense.setStartDate(f.parse("01/10/2014"));
        unsavedExpense.setEndDate(f.parse("20/11/2014"));
        unsavedExpense.setDescription("blaa blaa");
        unsavedExpense.setStatus(Expense.Status.SAVED);
        unsavedExpense.setModified(new Date());
        unsavedExpense.setAmount(200.55);

        session = createSession(user.getUsername(), PASSWORD, expense);

        initialExpenseCount = expenseRepository.count();
    }

    private MockHttpSession createSession(String username, String password, Expense exp) throws Exception {
        session = (MockHttpSession) mockMvc.perform(formLogin("/authenticate").user(username).password(password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn()
                .getRequest()
                .getSession();
        assertNotNull(session); 
        session.setAttribute("expense", exp);

        return session;
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        expenseRepository.deleteAll();
    }

    @Test
    public void expensePageWorks() throws Exception {
        mockMvc.perform(get("/expenses/" + expense.getId()).session(session))
                .andExpect(status().isOk());
    }

    private void testExpenseCanBeViewedBy(String username) throws Exception {
        MvcResult res = mockMvc.perform(get("/expenses/" + expense.getId()).with(user(username)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("expense"))
                .andExpect(view().name("expenses/edit")).andReturn();

        assertNotNull(res.getModelAndView().getModel().get("expense"));
    }

    @Test
    public void ownerCanViewExpense() throws Exception {
        testExpenseCanBeViewedBy(USERNAME);
    }

    @Test
    public void adminCanViewExpense() throws Exception {
        testExpenseCanBeViewedBy(ADMIN_USERNAME);
    }

    @Test
    public void supervisorCanViewExpense() throws Exception {
        testExpenseCanBeViewedBy(SUPERVISOR_USERNAME);
    }

    @Test
    public void NonOwnerUserCannotViewExpense() throws Exception {
        mockMvc.perform(get("/expenses/" + expense.getId()).with(user(USERNAME2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void expensePageModelHasExpense() throws Exception {
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

        assertEquals("The number of Expenses in the database should not change after updating an existing Expense.",
                initialExpenseCount, expenseRepository.count());

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
        session = createSession(USERNAME2, PASSWORD2, expense);

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
        expenseRepository.deleteAll();
        assertEquals(0, expenseRepository.count());

        session = createSession(USERNAME, PASSWORD, new Expense());
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        String url = "/expenses/";

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedExpense.getAmount().toString())
                .param("startDate", f.format(unsavedExpense.getStartDate()))
                .param("endDate", f.format(unsavedExpense.getEndDate()))
                .param("description", unsavedExpense.getDescription()))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertEquals("There should be a new Expense in the database after creating an Expense.",
                1, expenseRepository.count());

        Expense posted = expenseRepository.findAll().get(0);

        assertEquals("redirect:" + url + posted.getId(), res.getModelAndView().getViewName());

        assertEquals(unsavedExpense.getAmount(), posted.getAmount());
        assertEquals(unsavedExpense.getDescription(), posted.getDescription());
        assertEquals(unsavedExpense.getUser(), posted.getUser());
        assertEquals(f.format(unsavedExpense.getStartDate()), f.format(posted.getStartDate()));
        assertEquals(f.format(unsavedExpense.getEndDate()), f.format(posted.getEndDate()));
        assertEquals(Expense.Status.SAVED, posted.getStatus());
    }

    @Test
    public void postNewExpenseWithoutDetailsDoesNotCreateANewExpense() throws Exception {
        expenseRepository.deleteAll();
        assertEquals(0, expenseRepository.count());

        session = createSession(USERNAME, PASSWORD, new Expense());
        String url = "/expenses/";

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf()))
                .andReturn();

        assertEquals("There should not be a new Expense in the database after trying to create an empty Expense.",
                0, expenseRepository.count());

        assertEquals("expenses/new", res.getModelAndView().getViewName());
    }

    @Test
    public void postNewExpenseWithoutDescriptionDoesNotCreateANewExpense() throws Exception {
        expenseRepository.deleteAll();
        assertEquals(0, expenseRepository.count());

        session = createSession(USERNAME, PASSWORD, new Expense());
        String url = "/expenses/";
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedExpense.getAmount().toString())
                .param("startDate", f.format(unsavedExpense.getStartDate()))
                .param("endDate", f.format(unsavedExpense.getEndDate())))
                // Missing description
                .andReturn();

        assertEquals("There should not be a new Expense in the database after trying to create an Expense without a description.",
                0, expenseRepository.count());

        assertEquals("expenses/new", res.getModelAndView().getViewName());
    }

    @Test
    public void postNewExpenseWithoutStartDateDoesNotCreateANewExpense() throws Exception {
        expenseRepository.deleteAll();
        assertEquals(0, expenseRepository.count());

        session = createSession(USERNAME, PASSWORD, new Expense());
        String url = "/expenses/";
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedExpense.getAmount().toString())
                .param("status", unsavedExpense.getStatus().toString())
                // Missing startDate
                .param("endDate", f.format(unsavedExpense.getEndDate()))
                .param("description", unsavedExpense.getDescription()))
                .andReturn();

        assertEquals("There should not be a new Expense in the database after trying to create an Expense without a start date.",
                0, expenseRepository.count());

        assertEquals("expenses/new", res.getModelAndView().getViewName());
    }

    @Test
    public void postNewExpenseWithoutEndDateDoesNotCreateANewExpense() throws Exception {
        expenseRepository.deleteAll();
        assertEquals(0, expenseRepository.count());

        session = createSession(USERNAME, PASSWORD, new Expense());
        String url = "/expenses/";
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedExpense.getAmount().toString())
                .param("status", unsavedExpense.getStatus().toString())
                .param("startDate", f.format(unsavedExpense.getStartDate()))
                // Missing endDate
                .param("description", unsavedExpense.getDescription()))
                .andReturn();

        assertEquals("There should not be a new Expense in the database after trying to create an Expense without an end date.",
                0, expenseRepository.count());

        assertEquals("expenses/new", res.getModelAndView().getViewName());
    }

    @Test
    public void deleteExpenseByOwnerDeletesExpenseWithStatusSAVED() throws Exception {
        expense.setStatus(Expense.Status.SAVED);
        expense = expenseRepository.save(expense);
        assertEquals(initialExpenseCount, expenseRepository.count());

        String url = "/expenses/" + expense.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()));

        assertEquals("After deleting an Expense, there should be one less Expense in the database.",
                initialExpenseCount - 1, expenseRepository.count());

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

        assertEquals(initialExpenseCount + 1, expenseRepository.count());

        Long id = expense.getId();

        String url = "/expenses/" + id + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));

        assertEquals("After deleting an Expense, there should be one less Expenses in the database.",
                initialExpenseCount, expenseRepository.count());
        assertNull("After deleting an Expense, the deleted Expense should not be in the database.",
                expenseRepository.findOne(id));

        String actualDesc = expenseRepository.findAll().get(0).getDescription();

        assertEquals("After deleting the other Expense, the one left in the database should have the description '"
                + desc + "', but instead had '" + actualDesc + "'" , desc, actualDesc);

    }

    private void testDeleteFails(Expense expense) throws Exception {
        String url = "/expenses/" + expense.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is4xxClientError());
        assertEquals(initialExpenseCount, expenseRepository.count());
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