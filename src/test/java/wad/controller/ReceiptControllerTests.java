/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author teemu
 */



/*
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
import wad.domain.ExpenseRow;
import wad.domain.Receipt;
import wad.domain.User;
import wad.repository.CommentRepository;
import wad.repository.ExpenseRepository;
import wad.repository.ExpenseRowRepository;
import wad.repository.ReceiptRepository;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ReceiptControllerTests {
    
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
    private ReceiptRepository receiptRepository;
    
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
    public void postAddNewReceiptToTheExpense() throws Exception {
        receiptRepository.deleteAll();
        assertEquals(0, receiptRepository.count());

        expense = expenseRepository.save(expense);
        session = createSession(USERNAME, PASSWORD, expense);

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        String url = "/expenses/" + expense.getId() + "/rows";

        // Miten testillä tehdään lisääminen???
        MvcResult res = mockMvc.perform(post(url).session(session).with(csrf())
                .param("amount", unsavedRow.getAmount().toString())
                .param("date", f.format(unsavedRow.getDate()))
                .param("description", unsavedRow.getDescription()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertEquals("There should be a new Receipt in the database after creating a new one.",
                1, receiptRepository.count());

        Receipt posted = receiptRepository.findAll().get(0);

        assertEquals("redirect:/expenses/" + expense.getId(), res.getModelAndView().getViewName());

        // Toinen muutettava kohta.
        assertEquals(unsavedRow.getAmount(), posted.getAmount());
        assertEquals(unsavedRow.getDescription(), posted.getDescription());
        assertEquals(f.format(unsavedRow.getDate()), f.format(posted.getDate()));
        assertEquals(expense.getId(), posted.getExpense().getId());
    }    
}
*/