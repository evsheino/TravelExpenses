package wad;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import wad.domain.Authority;
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.UserRepository;

import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class SignInTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilterChain).build();
        this.webAppContext.getServletContext()
                .setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

        // Most of the tests don't require a user in the db
        // but add the user to the db here anyway for now.
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
        User user = userRepository.findByUsername(USERNAME);

        userRepository.delete(user);
    }

    @Test
    public void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void authenticatedUserCanAccessIndex() throws Exception {
        mockMvc.perform(get("/index").with(user(USERNAME)))
                .andExpect(status().isOk());
    }

    @Test
    public void userIsRedirectedAfterLogin() throws Exception {
        mockMvc.perform(formLogin("/authenticate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void userIsRedirectedAfterLogout() throws Exception {
        mockMvc.perform(logout())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

}
