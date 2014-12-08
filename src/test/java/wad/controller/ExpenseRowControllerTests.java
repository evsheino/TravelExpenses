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
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.ExpenseRowRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExpenseRowControllerTests {
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
    private ExpenseRowRepository rowRepository;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    private User user;
    private User user2;
    private User admin;
    private User supervisor;

    private Expense expense;
    private Expense unsavedExpense;

    private ExpenseRow row;
    private ExpenseRow unsavedRow;

    private long initialRowCount;

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
        expense.setStatus(Expense.Status.DRAFT);
        expense.setModified(new Date());

        unsavedExpense = new Expense();
        unsavedExpense.setUser(user);
        unsavedExpense.setStartDate(f.parse("01/10/2014"));
        unsavedExpense.setEndDate(f.parse("20/11/2014"));
        unsavedExpense.setDescription("blaa blaa");
        unsavedExpense.setStatus(Expense.Status.DRAFT);
        unsavedExpense.setModified(new Date());

        row = new ExpenseRow();
        row.setAmount(20.0);
        row.setDate(f.parse("21/09/2014"));
        row.setDescription("row description");

        unsavedRow = new ExpenseRow();
        unsavedRow.setAmount(34.5);
        unsavedRow.setDate(f.parse("22/09/2014"));
        unsavedRow.setDescription("unsaved row description");

        initialRowCount = rowRepository.count();
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
        rowRepository.deleteAll();
    }

    @Test
    public void postNewExpenseRowCreatesNewExpenseRow() throws Exception {
        rowRepository.deleteAll();
        assertEquals(0, rowRepository.count());

        expense = expenseRepository.save(expense);
        session = createSession(USERNAME, PASSWORD, expense);

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        String url = "/expenses/" + expense.getId() + "/rows";

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedRow.getAmount().toString())
                .param("date", f.format(unsavedRow.getDate()))
                .param("description", unsavedRow.getDescription()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertEquals("There should be a new ExpenseRow in the database after creating a new one.",
                1, expenseRepository.count());

        ExpenseRow posted = rowRepository.findAll().get(0);

        assertEquals("redirect:/expenses/" + expense.getId(), res.getModelAndView().getViewName());

        assertEquals(unsavedRow.getAmount(), posted.getAmount());
        assertEquals(unsavedRow.getDescription(), posted.getDescription());
        assertEquals(f.format(unsavedRow.getDate()), f.format(posted.getDate()));
        assertEquals(expense.getId(), posted.getExpense().getId());
    }

    @Test
    public void postNewExpenseRowWithoutDetailsDoesNotCreateANewExpenseRow() throws Exception {
        rowRepository.deleteAll();
        assertEquals(0, rowRepository.count());

        expense = expenseRepository.save(expense);
        session = createSession(USERNAME, PASSWORD, expense);

        String url = "/expenses/" + expense.getId() + "/rows";

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf()))
                .andReturn();

        assertEquals("There should not be a new Expense in the database after trying to create an empty Expense.",
                0, rowRepository.count());

        assertEquals("expenses/edit", res.getModelAndView().getViewName());
    }

    @Test
    public void postNewExpenseRowWithoutDescriptionDoesNotCreateANewExpenseRow() throws Exception {
        rowRepository.deleteAll();
        assertEquals(0, rowRepository.count());

        expense = expenseRepository.save(expense);
        session = createSession(USERNAME, PASSWORD, expense);

        String url = "/expenses/" + expense.getId() + "/rows";
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedRow.getAmount().toString())
                .param("date", f.format(unsavedRow.getDate())))
                .andReturn();

        assertEquals("There should not be a new ExpenseRow in the database after trying to create one without a description.",
                0, rowRepository.count());

        assertEquals("expenses/edit", res.getModelAndView().getViewName());
    }

    @Test
    public void postNewExpenseRowWithoutDateDoesNotCreateANewExpenseRow() throws Exception {
        rowRepository.deleteAll();
        assertEquals(0, rowRepository.count());

        expense = expenseRepository.save(expense);
        session = createSession(USERNAME, PASSWORD, expense);

        String url = "/expenses/" + expense.getId() + "/rows";
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedRow.getAmount().toString())
                .param("description", unsavedRow.getDescription()))
                .andReturn();

        assertEquals("There should not be a new ExpenseRow in the database after trying to create one without a date.",
                0, rowRepository.count());

        assertEquals("expenses/edit", res.getModelAndView().getViewName());
    }

    @Test
    public void deleteExpenseRowByOwnerDeletesExpenseRowWithStatusSAVED() throws Exception {
        expense.setStatus(Expense.Status.DRAFT);
        expense = expenseRepository.save(expense);
        row.setExpense(expense);
        row = rowRepository.save(row);
        expense = expenseRepository.findOne(expense.getId());

        session = createSession(USERNAME, PASSWORD, expense);

        initialRowCount = rowRepository.count();

        String url = "/expenses/" + expense.getId() + "/rows/" + row.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/" + expense.getId()));

        assertEquals("After deleting an ExpenseRow, there should be one less ExpenseRow in the database.",
                initialRowCount - 1, rowRepository.count());
    }

    @Test
    public void deleteExpenseRowDeletesCorrectExpenseRow() throws Exception {
        String desc = "do not delete this";

        expense.setStatus(Expense.Status.DRAFT);
        expense.setDescription("expense desc");
        expense = expenseRepository.save(expense);

        row.setExpense(expense);
        row.setDescription(desc);
        row = rowRepository.save(row);

        unsavedRow.setExpense(expense);
        unsavedRow.setDescription("DELETE THIS");
        unsavedRow = rowRepository.save(unsavedRow);

        expense = expenseRepository.findOne(expense.getId());
        session = createSession(USERNAME, PASSWORD, expense);

        long rowCount = initialRowCount + 2;
        assertEquals(rowCount, rowRepository.count());

        Long id = unsavedRow.getId();

        String url = "/expenses/" + expense.getId() + "/rows/" + unsavedRow.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/" + expense.getId()));

        assertEquals("After deleting an ExpenseRow, there should be one less ExpenseRow in the database.",
                rowCount - 1, rowRepository.count());
        assertNull("After deleting an Expense, the deleted Expense should not be in the database.",
                rowRepository.findOne(id));

        String actualDesc = rowRepository.findAll().get(0).getDescription();

        assertEquals("After deleting the other ExpenseRow, the one left in the database should have the description '"
                + desc + "', but instead had '" + actualDesc + "'" , desc, actualDesc);
    }

    private void testDeleteFails(ExpenseRow row) throws Exception {
        String url = "/expenses/" + expense.getId() + "/rows/" + row.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is4xxClientError());
        assertEquals(initialRowCount, rowRepository.count());
    }

    @Test
    public void deleteExpenseByNonOwnerNonAdminFails() throws Exception {
        expense.setUser(user2);
        expense = expenseRepository.save(expense);
        row.setExpense(expense);
        rowRepository.save(row);
        initialRowCount = rowRepository.count();

        session = createSession(USERNAME, PASSWORD, expense);

        testDeleteFails(row);
    }

    @Test
    public void deleteExpenseByOwnerWithStatusAPPROVEDFails() throws Exception {
        expense.setStatus(Expense.Status.APPROVED);
        expense = expenseRepository.save(expense);
        row.setExpense(expense);
        rowRepository.save(row);
        initialRowCount = rowRepository.count();

        session = createSession(USERNAME, PASSWORD, expense);

        testDeleteFails(row);
    }

    @Test
    public void deleteExpenseByOwnerWithStatusWAITINGFails() throws Exception {
        expense.setStatus(Expense.Status.SENT);
        expense = expenseRepository.save(expense);
        row.setExpense(expense);
        rowRepository.save(row);
        initialRowCount = rowRepository.count();

        session = createSession(USERNAME, PASSWORD, expense);

        testDeleteFails(row);
    }
}
