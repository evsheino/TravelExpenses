package wad.selenium;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
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
import org.springframework.web.context.WebApplicationContext;
import wad.Application;
import wad.domain.User;
import wad.repository.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SignInTest {

    private WebDriver driver;
    private final String LOGIN_URI = "http://localhost:8080/login";
    private ConfigurableApplicationContext context;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    UserRepository userRepository;

    @Before
    public void setUp() {
        this.context = SpringApplication.run(Application.class);
        this.driver = new HtmlUnitDriver();

        User user = new User();
        user.setName("John Doe");
        user.setUsername("user");
        user.setPassword("password");

        userRepository.save(user);
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
        context.close();
    }
    
    @Test
    public void loginPageWorks() throws Exception {
        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys("user");
        element = driver.findElement(By.name("password"));
        element.sendKeys("password");

        element.submit();

        assertEquals("http://localhost:8080/index", driver.getCurrentUrl());
    }

}
