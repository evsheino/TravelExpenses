package wad.selenium;

import java.text.SimpleDateFormat;
import org.junit.*;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;

import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import wad.Application;
import wad.domain.Authority;
import wad.domain.Comment;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.CommentRepository;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;
import wad.service.ExpenseService;
import wad.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApproveExpensesSeleniumTests {

    private final String DATE_FORMAT = "dd/MM/yyyy";

    private final String APPROVE_EXPENSES_URI = "http://localhost:8080/expenses/approve/";
    private final String DESCRIPTION = "blaa blaa blah";

    private final String LOGIN_URI = "http://localhost:8080/login";

    private static final String USER_1_NAME = "Test User 1";
    private static final String USER_1_USERNAME = "testuser_1";
    private static final String USER_1_PASSWORD = "password_1";

    private final String SUPERVISOR_NAME = "Ludwig Wittgenstein";
    private final String SUPERVISOR_USERNAME = "ludwig";
    private final String SUPERVISOR_PASSWORD = "tractatus";

    private WebDriver driver;
    private static ConfigurableApplicationContext context;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    ExpenseService expenseService;

    @Autowired
    private CommentRepository commentRepository;

    private Expense expense;
    private User user;
    private User supervisor;

    @BeforeClass
    public static void onetimeSetUp() {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("test");
        context = app.run();
    }

    @AfterClass
    public static void onetimeTearDown() {
        context.close();
    }

    @Before
    public void setUp() throws Exception {
        // Use FirefoxDriver for JavaScript support.
        driver = new FirefoxDriver();

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        user = userService.createUser(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, Authority.Role.USER);
        supervisor = userService.createUser(SUPERVISOR_NAME, SUPERVISOR_USERNAME, SUPERVISOR_PASSWORD, Authority.Role.SUPERVISOR);
        expense = expenseService.createExpense(user, f.parse("01/09/2014"), f.parse("02/09/2014"), 20.0, DESCRIPTION, Expense.Status.DRAFT);

        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(SUPERVISOR_USERNAME);
        element = driver.findElement(By.name("password"));
        element.sendKeys(SUPERVISOR_PASSWORD);

        element = driver.findElement(By.id("login-form"));
        element.submit();
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        expenseRepository.deleteAll();
        driver.quit();
    }

    @Test
    public void supervisorCanApproveExpense() throws Exception {
        String text = "new comment";

        driver.get(APPROVE_EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("approve-form-submit-label"));
        element.click();

        assertEquals("The user should be redirected to the Approve Expenses list. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                APPROVE_EXPENSES_URI + "list", driver.getCurrentUrl());

        assertEquals(Expense.Status.APPROVED, expenseRepository.findOne(expense.getId()).getStatus());
    }
    
    @Test
    public void supervisorCanRejectExpense() throws Exception {
        assertEquals(0, commentRepository.count());
        String text = "new comment";

        driver.get(APPROVE_EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("commentText"));
        element.clear();
        element.sendKeys(text);

        element = driver.findElement(By.id("reject-form"));
        element.submit();

        assertEquals("There should be one more Comment in the database after creating a new one.",
                1, commentRepository.count());

        Comment comment = commentRepository.findAll().get(0);

        assertEquals("The user should be redirected to the Approve Expenses list. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                APPROVE_EXPENSES_URI + "list", driver.getCurrentUrl());

        assertEquals(text, comment.getText());
        assertEquals(expense.getId(), comment.getExpense().getId());
    }
}
