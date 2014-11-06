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
import wad.domain.Authority;
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.UserRepository;

import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SignInTest {

    private final String LOGIN_URI = "http://localhost:8080/login";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private WebDriver driver;
    private ConfigurableApplicationContext context;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorityRepository authorityRepository;

    @Before
    public void setUp() {
        this.context = SpringApplication.run(Application.class);
        this.driver = new HtmlUnitDriver();

        User user = new User();
        user.setName("John Doe");
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user = userRepository.save(user);

        Authority authority = new Authority();
        authority.setAuthority(Authority.Auth.USER);
        authority.setUser(user);
        authority = authorityRepository.save(authority);

        user.setAuthorities(new ArrayList<Authority>());
        user.getAuthorities().add(authority);

        userRepository.save(user);

    }

    @After
    public void cleanup() {
        userRepository.delete(userRepository.findByUsername(USERNAME));
        context.close();
    }
    
    @Test
    public void loginPageWorks() throws Exception {
        driver.get(LOGIN_URI);

        assertTrue(driver.getPageSource().contains("Sign in"));

        WebElement element = driver.findElement(By.name("username"));
        element.sendKeys(USERNAME);
        element = driver.findElement(By.name("password"));
        element.sendKeys(PASSWORD);

        element.submit();

        assertEquals("http://localhost:8080/index", driver.getCurrentUrl());
    }

}
