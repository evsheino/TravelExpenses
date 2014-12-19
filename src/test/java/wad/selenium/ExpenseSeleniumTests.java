package wad.selenium;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import org.junit.*;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private static final String INDEX_URI = "http://localhost:8080/index";
    private static final String EXPENSES_URI = "http://localhost:8080/expenses/";

    private static final String XPATH_REPLACE_PATTERN = "REPLACE";
    private static final String INDEX_EXPENCES_LINK_XPATH_TEMPLATE = "//a[@href=\"/expenses?status="+ XPATH_REPLACE_PATTERN +"\"]";
    private static final String EXPENSES_TYPE_DROPDOWN_BUTTON_XPATH = "//button[@data-dropdown=\"expense-type\"]";
    private static final String EXPENSES_TYPE_DROPDOWN_LINK_XPATH_TEMPLATE = "//ul[@id=\"expense-type\"]/li[*]/a[@href=\"/expenses" + XPATH_REPLACE_PATTERN + "\"]";

    private static final String SUMMARY_DRAFT = "This is the description";
    private static final String SUMMARY_SENT = "blaa blaa blah";
    private static final String SUMMARY_REJECTED = "Please fix something";
    private static final String SUMMARY_APPROVED = "Yay this is good stuff.";

    private static final String LOGIN_URI = "http://localhost:8080/login";

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

    private User user;

    private Expense draftExpense;
    private Expense sentExpense;
    private Expense rejectedExpense;
    private Expense approvedExpense;

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

        user = userService.createUser(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, Authority.Role.ROLE_USER);
        draftExpense = expenseService.createExpense(user, DATE_FORMAT.parse("01/09/2014"), DATE_FORMAT.parse("02/09/2014"), SUMMARY_DRAFT, Expense.Status.DRAFT);
        sentExpense = expenseService.createExpense(user, DATE_FORMAT.parse("01/10/2014"), DATE_FORMAT.parse("02/10/2014"), SUMMARY_SENT, Expense.Status.SENT);
        rejectedExpense = expenseService.createExpense(user, DATE_FORMAT.parse("01/11/2014"), DATE_FORMAT.parse("02/11/2014"), SUMMARY_REJECTED, Expense.Status.REJECTED);
        approvedExpense = expenseService.createExpense(user, DATE_FORMAT.parse("01/12/2014"), DATE_FORMAT.parse("02/12/2014"), SUMMARY_APPROVED, Expense.Status.APPROVED);

        // Use FirefoxDriver for JavaScript support.
        driver = new FirefoxDriver();

        performLogin();

    }

    public void performLogin() {
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
    public void indexPageHasRecentExpensesFromAllCategories() throws Exception {
        driver.get(INDEX_URI);

        String src = driver.getPageSource();
        assertTrue(src.contains(SUMMARY_DRAFT));
        assertTrue(src.contains(SUMMARY_SENT));
        assertTrue(src.contains(SUMMARY_REJECTED));
        assertTrue(src.contains(SUMMARY_APPROVED));

        // View draft
        driver.get(INDEX_URI);
        WebElement element = driver.findElement(By.xpath(INDEX_EXPENCES_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, Expense.Status.DRAFT.toString())));
        element.click();
        src = driver.getPageSource();
        assertTrue(src.contains(SUMMARY_DRAFT));
        assertFalse(src.contains(SUMMARY_SENT));
        assertFalse(src.contains(SUMMARY_REJECTED));
        assertFalse(src.contains(SUMMARY_APPROVED));

        // View sent
        driver.get(INDEX_URI);
        element = driver.findElement(By.xpath(INDEX_EXPENCES_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, Expense.Status.SENT.toString())));
        element.click();
        src = driver.getPageSource();
        assertFalse(src.contains(SUMMARY_DRAFT));
        assertTrue(src.contains(SUMMARY_SENT));
        assertFalse(src.contains(SUMMARY_REJECTED));
        assertFalse(src.contains(SUMMARY_APPROVED));

        // View rejected
        driver.get(INDEX_URI);
        element = driver.findElement(By.xpath(INDEX_EXPENCES_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, Expense.Status.REJECTED.toString())));
        element.click();
        src = driver.getPageSource();
        assertFalse(src.contains(SUMMARY_DRAFT));
        assertFalse(src.contains(SUMMARY_SENT));
        assertTrue(src.contains(SUMMARY_REJECTED));
        assertFalse(src.contains(SUMMARY_APPROVED));

        // View approved
        driver.get(INDEX_URI);
        element = driver.findElement(By.xpath(INDEX_EXPENCES_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, Expense.Status.APPROVED.toString())));
        element.click();
        src = driver.getPageSource();
        assertFalse(src.contains(SUMMARY_DRAFT));
        assertFalse(src.contains(SUMMARY_SENT));
        assertFalse(src.contains(SUMMARY_REJECTED));
        assertTrue(src.contains(SUMMARY_APPROVED));
    }

    @Test
    public void expensesPageHasCategories() throws Exception {
        driver.get(EXPENSES_URI);

        String src = driver.getPageSource();
        assertTrue(src.contains(SUMMARY_DRAFT));
        assertTrue(src.contains(SUMMARY_SENT));
        assertTrue(src.contains(SUMMARY_REJECTED));
        assertTrue(src.contains(SUMMARY_APPROVED));

        // View draft
        WebElement element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_BUTTON_XPATH));
        element.click();
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, "?status=" + Expense.Status.DRAFT.toString())));
        element.click();
        src = driver.getPageSource();
        assertTrue(src.contains(SUMMARY_DRAFT));
        assertFalse(src.contains(SUMMARY_SENT));
        assertFalse(src.contains(SUMMARY_REJECTED));
        assertFalse(src.contains(SUMMARY_APPROVED));

        // View sent
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_BUTTON_XPATH));
        element.click();
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, "?status=" + Expense.Status.SENT.toString())));
        element.click();
        src = driver.getPageSource();
        assertFalse(src.contains(SUMMARY_DRAFT));
        assertTrue(src.contains(SUMMARY_SENT));
        assertFalse(src.contains(SUMMARY_REJECTED));
        assertFalse(src.contains(SUMMARY_APPROVED));

        // View rejected
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_BUTTON_XPATH));
        element.click();
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, "?status=" + Expense.Status.REJECTED.toString())));
        element.click();
        src = driver.getPageSource();
        assertFalse(src.contains(SUMMARY_DRAFT));
        assertFalse(src.contains(SUMMARY_SENT));
        assertTrue(src.contains(SUMMARY_REJECTED));
        assertFalse(src.contains(SUMMARY_APPROVED));

        // View approved
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_BUTTON_XPATH));
        element.click();
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, "?status=" + Expense.Status.APPROVED.toString())));
        element.click();
        src = driver.getPageSource();
        assertFalse(src.contains(SUMMARY_DRAFT));
        assertFalse(src.contains(SUMMARY_SENT));
        assertFalse(src.contains(SUMMARY_REJECTED));
        assertTrue(src.contains(SUMMARY_APPROVED));

        // View all
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_BUTTON_XPATH));
        element.click();
        element = driver.findElement(By.xpath(EXPENSES_TYPE_DROPDOWN_LINK_XPATH_TEMPLATE.replace(XPATH_REPLACE_PATTERN, "")));
        element.click();
        src = driver.getPageSource();
        assertTrue(src.contains(SUMMARY_DRAFT));
        assertTrue(src.contains(SUMMARY_SENT));
        assertTrue(src.contains(SUMMARY_REJECTED));
        assertTrue(src.contains(SUMMARY_APPROVED));

    }

    @Test
    public void expensePageHasCorrectInformation() throws Exception {
        driver.get(EXPENSES_URI + draftExpense.getId());

        String content = driver.getPageSource();

        assertTrue(content.contains(draftExpense.getSummary()));
        assertTrue(content.contains(draftExpense.getAmount().toString()));
        assertTrue(content.contains(DATE_FORMAT.format(draftExpense.getStartDate())));
        assertTrue(content.contains(DATE_FORMAT.format(draftExpense.getEndDate())));
        assertTrue(content.contains(draftExpense.getUser().getName()));
    }

    @Test
    public void expenseEditPageAllowsUserToEditExpense() throws Exception {
        assertEquals(4, expenseRepository.count());
        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";

        driver.get(EXPENSES_URI + draftExpense.getId());

        WebElement element = driver.findElement(By.name("summary"));
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

        assertEquals("The user should be redirected to the draftExpense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + draftExpense.getId(), driver.getCurrentUrl());

        assertEquals("There should be exactly 4 Expense in the database after editing expense.",
                4, expenseRepository.count());

        // Check that the Expense has been updated in the database.
        Expense updated = expenseRepository.findOne(draftExpense.getId());

        assertEquals(startDate, DATE_FORMAT.format(updated.getStartDate()));
        assertEquals(endDate, DATE_FORMAT.format(updated.getEndDate()));
        assertEquals(desc, updated.getSummary());

        // Check that the page has the updated Expense.
        String content = driver.getPageSource();

        assertTrue(content.contains(updated.getSummary()));
        assertTrue(content.contains(updated.getAmount().toString()));
        assertTrue(content.contains(DATE_FORMAT.format(updated.getStartDate())));
        assertTrue(content.contains(DATE_FORMAT.format(updated.getEndDate())));
        assertTrue(content.contains(updated.getUser().getName()));
    }

    @Test
    public void userCanDeleteExpenseWithStatusDRAFT() throws Exception {

        driver.get(EXPENSES_URI + draftExpense.getId());

        WebElement element = driver.findElement(By.id("delete-button"));
        element.click();

        // Wait for the confirmation popup
        Thread.sleep(1000);

        // Confirmation
        element = driver.findElement(By.id("delete-confirm-button"));
        element.click();

        assertEquals("The user should be redirected to the draftExpense list. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI, driver.getCurrentUrl() + "/");

        assertEquals("There should be 3 Expenses in the database after deleting the one Expense.",
                3, expenseRepository.count());
    }

    @Test
    public void userCanAddANewExpense() throws Exception {
        expenseRepository.deleteAll();

        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";

        driver.get(EXPENSES_URI + "new");

        WebElement element = driver.findElement(By.name("summary"));
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

        assertEquals("The user should be redirected to the draftExpense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals(startDate, DATE_FORMAT.format(expense.getStartDate()));
        assertEquals(endDate, DATE_FORMAT.format(expense.getEndDate()));
        assertEquals(desc, expense.getSummary());
        assertEquals(new BigDecimal(0), expense.getAmount());

        // Check that the page has the correct information.
        String content = driver.getPageSource();

        assertTrue(content.contains(expense.getSummary()));
        assertTrue(content.contains(expense.getAmount().toString()));
        assertTrue(content.contains(DATE_FORMAT.format(expense.getStartDate())));
        assertTrue(content.contains(DATE_FORMAT.format(expense.getEndDate())));
        assertTrue(content.contains(expense.getUser().getName()));
    }

    @Test
    public void userCanAddANewComment() throws Exception {
        String text = "new comment";

        driver.get(EXPENSES_URI + draftExpense.getId());

        WebElement element = driver.findElement(By.id("commentText"));
        element.clear();
        element.sendKeys(text);

        element = driver.findElement(By.id("add-comment-form"));
        element.submit();

        assertEquals("There should be one more Comment in the database after creating a new one.",
                1, commentRepository.count());

        Comment comment = commentRepository.findAll().get(0);

        assertEquals("The user should be redirected to the draftExpense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + draftExpense.getId(), driver.getCurrentUrl());

        assertEquals(text, comment.getText());

        // Check that the page has the correct information.
        String content = driver.getPageSource();

        assertTrue(content.contains(comment.getText()));
    }

    @Test
    public void expenseEditPageHasSendButtonThatTakesToConfirmSendPage() throws Exception {
        driver.get(EXPENSES_URI + draftExpense.getId());

        WebElement element = driver.findElement(By.id("send-button"));
        element.click();

        assertEquals("The user should be taken to the draftExpense send confirmation page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + draftExpense.getId() + "/send", driver.getCurrentUrl());

        String content = driver.getPageSource();

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");

        assertTrue(content.contains(draftExpense.getSummary()));
        assertTrue(content.contains(f.format(draftExpense.getStartDate())));
        assertTrue(content.contains(f.format(draftExpense.getEndDate())));
        assertTrue(content.contains(draftExpense.getAmount().toString()));
        assertTrue(content.contains(draftExpense.getUser().getName()));
    }

    @Test
    public void confirmSendPageAllowsUserToSendExpenseForApproval() throws Exception {
        driver.get(EXPENSES_URI + draftExpense.getId() + "/send");

        WebElement element = driver.findElement(By.id("send-form-submit-label"));
        element.click();

        assertEquals("The user should be taken to the draftExpense list. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI, driver.getCurrentUrl());

        draftExpense = expenseRepository.findOne(draftExpense.getId());
        assertEquals(draftExpense.getStatus(), Expense.Status.SENT);
    }

    @Test
    public void confirmSendPageHasABackButton() throws Exception {
        driver.get(EXPENSES_URI + draftExpense.getId() + "/send");

        WebElement element = driver.findElement(By.id("cancel-button"));
        element.click();

        assertEquals("The user should be taken to the draftExpense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + draftExpense.getId(), driver.getCurrentUrl());

        draftExpense = expenseRepository.findOne(draftExpense.getId());
        assertEquals(draftExpense.getStatus(), Expense.Status.DRAFT);
    }
}
