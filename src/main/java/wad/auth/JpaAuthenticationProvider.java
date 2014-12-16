package wad.auth;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import wad.domain.User;
import wad.repository.UserRepository;

@Component
public class JpaAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication a) throws AuthenticationException {
        String username = a.getPrincipal().toString();
        String password = a.getCredentials().toString();

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new AuthenticationException("Unable to authenticate user " + username) {
            };
        }

        if (!BCrypt.hashpw(password, user.getSalt()).equals(user.getPassword())) {
            throw new AuthenticationException("Unable to authenticate user " + username) {
            };
        }

        if(user.getAuthorities().isEmpty()) {
            throw new AuthenticationException("User " + username + " lacks sufficient authorities to log in the system.") {};
        }

        return new UsernamePasswordAuthenticationToken(user.getUsername(), password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> type) {
        return true;
    }
}
