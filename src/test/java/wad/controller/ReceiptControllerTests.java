/**
 *
 * @author teemu
 */


package wad.controller;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
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
import wad.domain.Receipt;
import wad.domain.User;
import wad.repository.ExpenseRepository;
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
    private ReceiptRepository receiptRepository;
    
    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    private User user;
    private User user2;
    private User admin;
    private User supervisor;

    private Expense expense;

    private ExpenseRow row;

    private MockMultipartFile file;
    private Receipt receipt;
    
    private MockHttpSession session;

    private long initialReceiptCount;

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
        expense = expenseRepository.save(expense);

        session = createSession(USERNAME, PASSWORD, expense);

        String filename = "receipt.pdf";
        String name = "file";
        String type = "application/pdf";
        String content = "this is not a valid pdf but whatever";

        file = new MockMultipartFile(name, filename, type, content.getBytes());

        receipt = new Receipt();
        receipt.setContent(file.getBytes());
        receipt.setExpense(expense);
        receipt.setMediaType(file.getContentType());
        receipt.setName(file.getName());
        receipt.setSize(file.getSize());
        receipt.setSubmitted(new Date());

        initialReceiptCount = receiptRepository.count();
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
        receiptRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void userCanGetReceipt() throws Exception {
        receipt = receiptRepository.save(receipt);

        MvcResult res = mockMvc.perform(get("/expenses/" + expense.getId() + "/receipts/" + receipt.getId()).session(session))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(new String(file.getBytes()), res.getResponse().getContentAsString());
    }
    
    @Test
    public void postAddNewReceiptToTheExpense() throws Exception {
        receiptRepository.deleteAll();
        assertEquals(0, receiptRepository.count());

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        String url = "/expenses/" + expense.getId() + "/receipts";

        MvcResult res = mockMvc.perform(fileUpload(url).file(file).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/" + expense.getId()))
                .andReturn();

        assertEquals("There should be a new Receipt in the database after creating a new one.",
                1, receiptRepository.count());

        Receipt posted = receiptRepository.findAll().get(0);

        assertEquals(file.getContentType(), posted.getMediaType());
        assertEquals(expense, posted.getExpense());
        assertEquals(file.getSize(), (long) posted.getSize());
        assertEquals(f.format(new Date()), f.format(posted.getSubmitted()));
        assertEquals(new String(file.getBytes()), new String(posted.getContent()));
    }    

    @Test
    public void uploadWithUnsupportedContentTypeIsNowSaved() throws Exception {
        assertEquals(initialReceiptCount, receiptRepository.count());

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        String url = "/expenses/" + expense.getId() + "/receipts";

        file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "virus".getBytes());

        MvcResult res = mockMvc.perform(fileUpload(url).file(file).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/" + expense.getId()))
                .andReturn();

        assertEquals("There should not be a new Receipt in the database after trying to upload one with an unsupported content type.",
                initialReceiptCount, receiptRepository.count());
    }    

    @Test
    public void deleteReceiptDeletesReceipt() throws Exception {
        receipt = receiptRepository.save(receipt);

        assertEquals(initialReceiptCount + 1, receiptRepository.count());

        String url = "/expenses/" + expense.getId() + "/receipts/" + receipt.getId() + "/delete";
        mockMvc.perform(post(url).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/" + expense.getId()));

        assertEquals("After deleting a Receipt, there should be one less Receipt in the database.",
                initialReceiptCount, receiptRepository.count());
    }
}