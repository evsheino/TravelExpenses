package wad.selenium;

import org.junit.*;
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class AdminSeleniumTests {

    private final String LOGIN_URI = "http://localhost:8080/login";

    private final String DATE_FORMAT = "dd/MM/yyyy";

    private final String ADMIN_BASE_URI = "http://localhost:8080/admin";
    private final String USERS_URI = ADMIN_BASE_URI + "/users";

    private final String DESCRIPTION = "blaa blaa blah";

    private static final String USER_1_NAME = "Test User 1";
    private static final String USER_1_USERNAME = "testuser_1";
    private static final String USER_1_PASSWORD = "password_1";

    private static final String ADMIN_1_NAME = "Admin User 1";
    private static final String ADMIN_1_USERNAME = "admin_user_1";
    private static final String ADMIN_1_PASSWORD = "admin_user_1";

    private WebDriver driver;
    private static ConfigurableApplicationContext context;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;


    private User user;
    private User admin;

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
        admin = userService.createUser(ADMIN_1_NAME, ADMIN_1_USERNAME, ADMIN_1_PASSWORD, Authority.Role.ROLE_USER, Authority.Role.ROLE_ADMIN);
        // Use FirefoxDriver for JavaScript support.
        driver = new FirefoxDriver();
        performLogin(ADMIN_1_USERNAME, ADMIN_1_PASSWORD);

    }

    private void performLogin(String username, String password) {
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
        driver.quit();
    }

    @Test
    public void adminHasListOfUsersAndEditUserDetails() throws Exception {
        driver.get(USERS_URI);
        String source = driver.getPageSource();
        assertTrue(source.contains("/admin/user/"+ user.getId()));
        assertTrue(source.contains("/admin/user/"+ admin.getId()));

        WebElement element = driver.findElement(By.xpath("//a[@href=\"/admin/user/"+ user.getId() +"\"]"));
        element.click();

        assertEquals(ADMIN_BASE_URI + "/user/"+ user.getId(), driver.getCurrentUrl());

        element = driver.findElement(By.id("force-password"));
        assertFalse(element.isSelected());
        element.click();

        element = driver.findElement(By.id("role-user"));
        assertTrue(element.isSelected());

        element = driver.findElement(By.id("role-supervisor"));
        assertFalse(element.isSelected());
        element.click();

        element = driver.findElement(By.id("role-admin"));
        assertFalse(element.isSelected());
        element.click();

        element = driver.findElement(By.id("admin-user-data-form-submit"));
        element.click();


        assertEquals(USERS_URI, driver.getCurrentUrl());

        User savedUser = userRepository.findOne(user.getId());
        assertTrue(savedUser.getPasswordExpired());
        assertTrue(savedUser.isUser());
        assertTrue(savedUser.isSupervisor());
        assertTrue(savedUser.isAdmin());
    }

}
