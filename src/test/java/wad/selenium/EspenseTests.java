package wad.selenium;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.*;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;

import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
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
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;
import wad.service.ExpenseService;
import wad.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EspenseTests {

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
    public void setUp() {
        // Use FirefoxDriver for JavaScript support.
        driver = new FirefoxDriver();

        user = userService.createUser(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, Authority.Role.USER);
        expense = expenseService.createExpense(user, new Date(), new Date(), 20.0, DESCRIPTION);

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
        userRepository.deleteAll();
        expenseRepository.deleteAll();
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
        String desc = "new description";
        String startDate = "09/09/2010";
        String endDate = "21/09/2010";
        String amount = "200";

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
        element = driver.findElement(By.name("amount"));
        element.clear();
        element.sendKeys(amount);

        element = driver.findElement(By.id("edit-form-submit"));
        element.click();

        assertEquals("The user should be redirected to the expense's page. Instead, was redirected to " + driver.getCurrentUrl() + ".",
                EXPENSES_URI + expense.getId(), driver.getCurrentUrl());

        assertEquals("There should be exactly 1 Expense in the database after editing the only existing Expense.",
                1, expenseRepository.count());

        // Check that the Expense has been updated in the database.
        Expense updated = expenseRepository.findOne(expense.getId());

        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        assertEquals(f.parse(startDate), updated.getStartDate());
        assertEquals(f.parse(endDate), updated.getEndDate());
        assertEquals(desc, updated.getDescription());
        assertEquals(200, updated.getAmount(), 0.001);

        // Check that the page has the updated Expense.
        String content = driver.getPageSource();

        assertTrue(content.contains(updated.getDescription()));
        assertTrue(content.contains(updated.getAmount().toString()));
        assertTrue(content.contains(f.format(updated.getStartDate())));
        assertTrue(content.contains(f.format(updated.getEndDate())));
        assertTrue(content.contains(updated.getUser().getName()));
    }
}
