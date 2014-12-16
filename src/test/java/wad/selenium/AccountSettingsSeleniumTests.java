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
import wad.domain.User;
import wad.repository.UserRepository;
import wad.service.UserService;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class AccountSettingsSeleniumTests {

    private final String LOGIN_URI = "http://localhost:8080/login";
    private final String LOGOUT_URI = "http://localhost:8080/logout";

    private final String ACCOUNT_SETTINGS_URI = "http://localhost:8080/account";
    //private final String USERS_URI = ADMIN_BASE_URI + "/users";

    private static final String USER_1_NAME = "Test User 1";
    private static final String USER_1_USERNAME = "testuser_1";
    private static final String USER_1_PASSWORD = "password_1";
    private static final String USER_1_NEW_PASSWORD = "new_password_1";

    private WebDriver driver;
    private static ConfigurableApplicationContext context;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;

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
        user = userService.createUser(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, Authority.Role.ROLE_USER);
        // Use FirefoxDriver for JavaScript support.
        driver = new FirefoxDriver();
        performLogin(USER_1_USERNAME, USER_1_PASSWORD);

    }

    private void performLogin(String username, String password) {
        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(username);
        element = driver.findElement(By.name("password"));
        element.sendKeys(password);

        element = driver.findElement(By.id("login-form"));
        element.submit();
    }

    private void performLogout() {
        driver.get(LOGOUT_URI);
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        driver.quit();
    }

    @Test
    public void changePassword() throws Exception {

        // Go to account settings page
        driver.get(ACCOUNT_SETTINGS_URI);

        // Change password
        WebElement element = driver.findElement(By.id("password"));
        element.sendKeys(USER_1_NEW_PASSWORD);
        element = driver.findElement(By.id("confirm-password"));
        element.sendKeys(USER_1_NEW_PASSWORD);
        element = driver.findElement(By.id("admin-user-data-form-submit"));
        element.click();

        // Logout
        performLogout();

        // Login with old password fails
        performLogin(USER_1_USERNAME, USER_1_PASSWORD);
        assertTrue(driver.getPageSource().contains("Invalid username and password."));
        //assertEquals(ACCOUNT_SETTINGS_URI +"?error", driver.getCurrentUrl());

        // Login with new password
        performLogin(USER_1_USERNAME, USER_1_NEW_PASSWORD);

        // Go to account settings page
        driver.get(ACCOUNT_SETTINGS_URI);
        assertEquals(ACCOUNT_SETTINGS_URI, driver.getCurrentUrl());
        String source = driver.getPageSource();
        assertTrue(source.contains("Account settings"));
        assertTrue(source.contains("Account info"));

    }

}
