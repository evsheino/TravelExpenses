package wad;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import wad.domain.Authority;
import wad.repository.AuthorityRepository;
import wad.repository.UserRepository;
import wad.service.UserService;

import wad.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class UserServiceTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    private User user;

    @Before
    public void setUp() {
        assertEquals(0, userRepository.count());

        this.user = new User();

        this.user.setName("John Doe");
        this.user.setPassword("password");
        this.user.setUsername("johnd");
    }

    @After
    public void cleanup() {
        userRepository.deleteAll();
    }

    // TODO: Add test for getCurrentUser

    @Test
    public void saveUserCreatesANewUserWhenUserDoesNotExist() throws Exception {
        user = userService.saveUser(user, Authority.Role.USER);

        User fetchedUser = userRepository.findOne(user.getId());

        assertEquals(1, userRepository.count());
        assertEquals(fetchedUser, user);
    }

    @Test
    public void saveUserUpdatesUserWhenUserExists() throws Exception {
        User repoUser = userRepository.save(user);

        User serviceUser = userService.saveUser(user, Authority.Role.USER);

        assertEquals(1, userRepository.count());
        assertEquals(repoUser, serviceUser);
    }

    @Test
    public void createUserCreatesANewUser() throws Exception {
        user = userService.createUser(user.getName(), user.getUsername(), "password", Authority.Role.USER);

        assertEquals(1, userRepository.count());

        User fetchedUser = userRepository.findOne(user.getId());

        assertEquals(fetchedUser, user);
    }

    @Test
    public void deleteUserDeletesUser() throws Exception {
        user = userRepository.save(user);

        User user2 = new User();
        user2.setName("Immanuel Kant");
        user2.setPassword("password");
        user2.setUsername("kant");

        userRepository.save(user2);

        assertEquals(2, userRepository.count());
        userService.deleteUser(user.getUsername());

        assertEquals(1, userRepository.count());
        assertNull(userRepository.findOne(user.getId()));
    }
}
