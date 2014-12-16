package wad.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import wad.domain.Authority;
import wad.domain.User;
import wad.repository.UserRepository;
import wad.service.UserService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;


/**
 * User: Niko
 * Date: 8.12.2014
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {

    private static final String NAME = "name";
    private static final String USERNAME = "username";

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminController adminController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSaveNewUser() {
        Authority.Role[] roles = {Authority.Role.ROLE_USER, Authority.Role.ROLE_ADMIN};
        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);

        adminController.saveNewUser(NAME, USERNAME, roles);

        verify(userService).saveUser(user.capture(), eq(Authority.Role.ROLE_USER), eq(Authority.Role.ROLE_ADMIN));
        assertEquals(NAME, user.getValue().getName());
        assertEquals(USERNAME, user.getValue().getUsername());
        assertTrue(user.getValue().getPasswordExpired());
    }

    @Test
    public void testSaveUser() {
        User user = mock(User.class);
        Authority.Role[] roles = {Authority.Role.ROLE_USER, Authority.Role.ROLE_ADMIN};
        ArgumentCaptor<User> uc = ArgumentCaptor.forClass(User.class);

        when(userRepository.findOne(anyLong())).thenReturn(user);

        adminController.saveUser(1L, NAME, USERNAME, false, roles);

        verify(user, times(1)).setPasswordExpired(false);
        verify(userService).saveUser(uc.capture(), eq(Authority.Role.ROLE_USER), eq(Authority.Role.ROLE_ADMIN));

        assertFalse(uc.getValue().getPasswordExpired()); // Supposed to change

    }

}

