package wad.selenium;

import java.math.BigDecimal;
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
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.ExpenseRowRepository;
import wad.repository.UserRepository;
import wad.service.ExpenseService;
import wad.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ExpenseRowSeleniumTests {

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
    ExpenseRowRepository rowRepository;

    @Autowired
    ExpenseService expenseService;

    private Expense expense;
    private ExpenseRow row;
    private User user;

    private long initialRowCount;

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
        expense = expenseService.createExpense(user, f.parse("01/09/2014"), f.parse("04/09/2014"), 20.0, DESCRIPTION, Expense.Status.DRAFT);

        row = new ExpenseRow();
        row.setAmount(new BigDecimal("20.5"));
        row.setDate(f.parse("02/09/2014"));
        row.setDescription("taxi fare");
        row.setExpense(expense);
        rowRepository.save(row);

        expense = expenseService.getExpense(expense.getId());

        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(USER_1_USERNAME);
        element = driver.findElement(By.name("password"));
        element.sendKeys(USER_1_PASSWORD);

        element = driver.findElement(By.id("login-form"));
        element.submit();

        initialRowCount = rowRepository.count();

    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        expenseRepository.deleteAll();
        rowRepository.deleteAll();
        driver.quit();
    }
    
    @Test
    public void expensePageHasCorrectRowInformation() throws Exception {
        driver.get(EXPENSES_URI + expense.getId());

        String content = driver.getPageSource();

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        assertTrue(content.contains(row.getDescription()));
        assertTrue(content.contains(row.getAmount().toString()));
        assertTrue(content.contains(f.format(row.getDate())));
    }

    @Test
    public void expenseEditPageAllowsUserToEditRow() throws Exception {
        assertEquals(1, rowRepository.count());
        String desc = "new description";
        String date = "03/09/2014";
        BigDecimal amount = new BigDecimal("12.5");

        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.name("expenseRows[0].description"));
        element.clear();
        element.sendKeys(desc);
        element = driver.findElement(By.name("expenseRows[0].date"));
        element.clear();
        element.sendKeys(date);
        element = driver.findElement(By.name("expenseRows[0].amount"));
        element.clear();
        element.sendKeys(amount.toString());

        element = driver.findElement(By.id("edit-form-submit"));
        element.click();

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals("There should be exactly 1 ExpenseRow in the database after editing the only existing one.",
                1, rowRepository.count());

        // Check that the Expense has been updated in the database.
        ExpenseRow updated = rowRepository.findOne(row.getId());

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        assertEquals(date, f.format(updated.getDate()));
        assertEquals(amount.stripTrailingZeros(), updated.getAmount().stripTrailingZeros());
        assertEquals(desc, updated.getDescription());

        // Check that the page has the updated Expense.
        String content = driver.getPageSource();

        assertTrue(content.contains(updated.getDescription()));
        assertTrue(content.contains(updated.getAmount().toString()));
        assertTrue(content.contains(f.format(updated.getDate())));
    }

    @Test
    public void userCanDeleteExpenseRowFromExpenseWithStatusDRAFT() throws Exception {

        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("delete-row-confirm-button-" + row.getId()));
        element.click();

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals("There should be no ExpenseRows in the database after deleting the only existing one.",
                0, rowRepository.count());
    }

    @Test
    public void userCanAddANewExpenseRow() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);

        expenseRepository.deleteAll();
        expense = expenseService.createExpense(user, f.parse("01/09/2014"), f.parse("04/09/2014"), 20.0, DESCRIPTION, Expense.Status.DRAFT);

        assertEquals(0, rowRepository.count());

        String desc = "new description";
        String date = "03/09/2014";
        BigDecimal amount = new BigDecimal("30.4");

        driver.get(EXPENSES_URI + expense.getId());

        WebElement element = driver.findElement(By.id("row-add-description"));
        element.clear();
        element.sendKeys(desc);
        element = driver.findElement(By.id("row-add-date"));
        element.clear();
        element.sendKeys(date);
        element = driver.findElement(By.id("row-add-amount"));
        element.clear();
        element.sendKeys(amount.toString());

        element = driver.findElement(By.id("add-row-form"));
        element.submit();

        assertEquals("There should be one more ExpenseRows in the database after creating a new one.",
                1, rowRepository.count());

        row = rowRepository.findAll().get(0);

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals(date, f.format(row.getDate()));
        assertEquals(amount.stripTrailingZeros(), row.getAmount().stripTrailingZeros());
        assertEquals(desc, row.getDescription());

        // Check that the page has the correct information.
        String content = driver.getPageSource();

        assertTrue(content.contains(row.getDescription()));
        assertTrue(content.contains(row.getAmount().toString()));
        assertTrue(content.contains(f.format(row.getDate())));
    }
}
