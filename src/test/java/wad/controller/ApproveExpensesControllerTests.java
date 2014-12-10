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
import org.springframework.transaction.annotation.Transactional;
import wad.Application;
import wad.domain.Authority;
import wad.domain.Comment;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.CommentRepository;
import wad.repository.ExpenseRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApproveExpensesControllerTests {
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
    private CommentRepository commentRepository;

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

        user = userService.createUser(NAME, USERNAME, PASSWORD, Authority.Role.ROLE_USER);
        user2 = userService.createUser(NAME2, USERNAME2, PASSWORD2, Authority.Role.ROLE_USER);
        admin = userService.createUser(ADMIN_NAME, ADMIN_USERNAME, ADMIN_PASSWORD, Authority.Role.ROLE_ADMIN);
        supervisor = userService.createUser(SUPERVISOR_NAME, SUPERVISOR_USERNAME, SUPERVISOR_PASSWORD, Authority.Role.ROLE_SUPERVISOR);

        userRepository.save(user);

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        expense = new Expense();
        expense.setUser(user);
        expense.setStartDate(f.parse("20/09/2014"));
        expense.setEndDate(f.parse("29/09/2014"));
        expense.setDescription("blaa blaa");
        expense.setStatus(Expense.Status.SENT);
        expense.setModified(new Date());
        expense = expenseRepository.save(expense);

        unsavedExpense = new Expense();
        unsavedExpense.setUser(user);
        unsavedExpense.setStartDate(f.parse("01/10/2014"));
        unsavedExpense.setEndDate(f.parse("20/11/2014"));
        unsavedExpense.setDescription("blaa blaa");
        unsavedExpense.setStatus(Expense.Status.DRAFT);
        unsavedExpense.setModified(new Date());

        session = createSession(supervisor.getUsername(), SUPERVISOR_PASSWORD, expense);

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
        commentRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void supervisorCanApproveExpense() throws Exception {
        String url = "/expenses/approve/" + expense.getId() + "/approve";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/approve/list"));

        expense = expenseRepository.findOne(expense.getId());

        assertEquals(Expense.Status.APPROVED, expense.getStatus());
    }

    @Test
    public void supervisorCanRejectExpense() throws Exception {
        assertEquals(0, commentRepository.count());

        String text = "reason for rejection";
        String url = "/expenses/approve/" + expense.getId() + "/reject";
        mockMvc.perform(post(url).session(session).with(csrf())
                .param("commentText", text))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/approve/list"));

        assertEquals(1, commentRepository.count());

        expense = expenseRepository.findOne(expense.getId());

        assertEquals(Expense.Status.REJECTED, expense.getStatus());
        assertEquals(text, expense.getComments().get(0).getText());
    }

    @Test
    public void userCannotApproveExpense() throws Exception {
        session = createSession(USERNAME, PASSWORD, expense);

        String url = "/expenses/approve/" + expense.getId() + "/approve";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void userCannotRejectExpense() throws Exception {
        session = createSession(USERNAME, PASSWORD, expense);

        String url = "/expenses/approve/" + expense.getId() + "/reject";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
