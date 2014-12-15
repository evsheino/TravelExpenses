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
public class ExpenseSeleniumTests {

    private final String DATE_FORMAT = "dd/MM/yyyy";

    private final String EXPENSES_URI = "http://localhost:8080/expenses/";
    private final String DESCRIPTION = "blaa blaa blah";

    private final String LOGIN_URI = "http://localhost:8080/login";

    private static final String USER_1_NAME = "Test User 1";
    private static final String USER_1_USERNAME = "testuser_1";
    private static final String USER_1_PASSWORD = "password_1";

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

        user = userService.createUser(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, Authority.Role.ROLE_USER);
        expense = expenseService.createExpense(user, f.parse("01/09/2014"), f.parse("02/09/2014"), 20.0, DESCRIPTION, Expense.Status.DRAFT);

        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(USER_1_USERNAME);
        element = driver.findElement(By.name("password"));
        element.sendKeys(USER_1_PASSWORD);

        element = driver.findElement(By.id("login-form"));
        element.submit();
    }

    @After
    public void cleanup() {
        commentRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        driver.quit();
    }
    
    @Test
    public void expensePageHasCorrectInformation() throws Exception {
        driver.get(EXPENSES_URI + expense.getId());

        String content = driver.getPageSource();

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        assertTrue(content.contains(expense.getDescription()));
        assertTrue(content.contains(expense.getAmount().toString()));
        assertTrue(content.contains(f.format(expense.getStartDate())));
        assertTrue(content.contains(f.format(expense.getEndDate())));
        assertTrue(content.contains(expense.getUser().getName()));
    }

    @Test
    public void expenseEditPageAllowsUserToEditExpense() throws Exception {
        assertEquals(1, expenseRepository.count());
        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";

        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.name("description"));
        element.clear();
        element.sendKeys(desc);
        element = driver.findElement(By.name("startDate"));
        element.clear();
        element.sendKeys(startDate);
        element = driver.findElement(By.name("endDate"));
        element.clear();
        element.sendKeys(endDate);

        element = driver.findElement(By.id("edit-form-submit"));
        element.click();

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals("There should be exactly 1 Expense in the database after editing the only existing Expense.",
                1, expenseRepository.count());

        // Check that the Expense has been updated in the database.
        Expense updated = expenseRepository.findOne(expense.getId());

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        assertEquals(startDate, f.format(updated.getStartDate()));
        assertEquals(endDate, f.format(updated.getEndDate()));
        assertEquals(desc, updated.getDescription());

        // Check that the page has the updated Expense.
        String content = driver.getPageSource();

        assertTrue(content.contains(updated.getDescription()));
        assertTrue(content.contains(updated.getAmount().toString()));
        assertTrue(content.contains(f.format(updated.getStartDate())));
        assertTrue(content.contains(f.format(updated.getEndDate())));
        assertTrue(content.contains(updated.getUser().getName()));
    }

    @Test
    public void userCanDeleteExpenseWithStatusSAVED() throws Exception {

        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("delete-button"));
        element.click();

        // Wait for the confirmation popup
        Thread.sleep(1000);

        // Confirmation
        element = driver.findElement(By.id("delete-confirm-button"));
        element.click();

        assertEquals("The user should be redirected to the expense list. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI, driver.getCurrentUrl() + "/");

        assertEquals("There should be no Expenses in the database after deleting the only existing Expense.",
                0, expenseRepository.count());
    }

    @Test
    public void userCanAddANewExpense() throws Exception {
        expenseRepository.deleteAll();

        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";

        driver.get(EXPENSES_URI + "new");

        WebElement element = driver.findElement(By.name("description"));
        element.clear();
        element.sendKeys(desc);
        element = driver.findElement(By.name("startDate"));
        element.clear();
        element.sendKeys(startDate);
        element = driver.findElement(By.name("endDate"));
        element.clear();
        element.sendKeys(endDate);

        element = driver.findElement(By.id("add-expense-form"));
        element.submit();

        assertEquals("There should be one more Expense in the database after creating a new one.",
                1, expenseRepository.count());

        Expense expense = expenseRepository.findAll().get(0);

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        assertEquals(startDate, f.format(expense.getStartDate()));
        assertEquals(endDate, f.format(expense.getEndDate()));
        assertEquals(desc, expense.getDescription());
        assertEquals(0, expense.getAmount(), 0.001);

        // Check that the page has the correct information.
        String content = driver.getPageSource();

        assertTrue(content.contains(expense.getDescription()));
        assertTrue(content.contains(expense.getAmount().toString()));
        assertTrue(content.contains(f.format(expense.getStartDate())));
        assertTrue(content.contains(f.format(expense.getEndDate())));
        assertTrue(content.contains(expense.getUser().getName()));
    }

    @Test
    public void userCanAddANewComment() throws Exception {
        String text = "new comment";

        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("commentText"));
        element.clear();
        element.sendKeys(text);

        element = driver.findElement(By.id("add-comment-form"));
        element.submit();

        assertEquals("There should be one more Comment in the database after creating a new one.",
                1, commentRepository.count());

        Comment comment = commentRepository.findAll().get(0);

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals(text, comment.getText());

        // Check that the page has the correct information.
        String content = driver.getPageSource();

        assertTrue(content.contains(comment.getText()));
    }

    @Test
    public void expenseEditPageHasSendButtonThatTakesToConfirmSendPage() throws Exception {
        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("send-button"));
        element.click();

        assertEquals("The user should be taken to the expense send confirmation page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId() + "/send", driver.getCurrentUrl());

        String content = driver.getPageSource();

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");

        assertTrue(content.contains(expense.getDescription()));
        assertTrue(content.contains(f.format(expense.getStartDate())));
        assertTrue(content.contains(f.format(expense.getEndDate())));
        assertTrue(content.contains(expense.getAmount().toString()));
        assertTrue(content.contains(expense.getUser().getName()));
    }

    @Test
    public void confirmSendPageAllowsUserToSendExpenseForApproval() throws Exception {
        driver.get(EXPENSES_URI + expense.getId() + "/send");

        WebElement element = driver.findElement(By.id("send-form-submit-label"));
        element.click();

        assertEquals("The user should be taken to the expense list. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI, driver.getCurrentUrl());

        expense = expenseRepository.findOne(expense.getId());
        assertEquals(expense.getStatus(), Expense.Status.SENT);
    }

    @Test
    public void confirmSendPageHasABackButton() throws Exception {
        driver.get(EXPENSES_URI + expense.getId() + "/send");

        WebElement element = driver.findElement(By.id("cancel-button"));
        element.click();

        assertEquals("The user should be taken to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        expense = expenseRepository.findOne(expense.getId());
        assertEquals(expense.getStatus(), Expense.Status.DRAFT);
    }
}
