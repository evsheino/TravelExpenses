package wad.selenium;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.UserRepository;
import wad.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SignInSeleniumTests {

    private final String LOGIN_URI = "http://localhost:8080/login";

    private static final String USER_1_NAME = "Test User 1";
    private static final String USER_1_USERNAME = "testuser_1";
    private static final String USER_1_PASSWORD = "password_1";

    private WebDriver driver;
    private static ConfigurableApplicationContext context;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @BeforeClass
    public static void onetimeSetUp() {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("test");
        context = app.run();
    }

    @AfterClass
    public static void ontimeTearDown() {
        context.close();
    }

    @Before
    public void setUp() {

        this.driver = new HtmlUnitDriver();

        userService.createUser(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, Authority.Role.ROLE_USER);

    }

    @After
    public void cleanup() {
        userService.deleteUser(USER_1_USERNAME);
    }
    
    @Test
    public void loginPageWorks() throws Exception {
        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(USER_1_USERNAME);
        element = driver.findElement(By.name("password"));
        element.sendKeys(USER_1_PASSWORD);

        element = driver.findElement(By.id("login-form"));
        element.submit();

        assertEquals("http://localhost:8080/", driver.getCurrentUrl());
    }

    @Test
    public void logoutWorks() throws Exception {
        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(USER_1_USERNAME);
        element = driver.findElement(By.name("password"));
        element.sendKeys(USER_1_PASSWORD);

        element = driver.findElement(By.id("login-form"));
        element.submit();

        assertEquals("http://localhost:8080/", driver.getCurrentUrl());
        assertTrue(driver.getPageSource().contains("Log out"));

        element = driver.findElement(By.id("logout-form"));
        element.submit();

        assertTrue(driver.getPageSource().contains("Sign in"));

    }

}
