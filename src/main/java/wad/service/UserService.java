package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import wad.domain.Authority;
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.UserRepository;

import java.util.ArrayList;

/**
 * Created by nryytty@cs on 10.11.2014.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    public User createUser(String name, String username, String password, Authority.Role... roles) {
        User user = new User(name, username, password);
        user = userRepository.save(user);
        user.setAuthorities(new ArrayList<Authority>());

        for(Authority.Role role : roles) {
            Authority authority  = new Authority();
            authority.setUser(user);
            authority.setAuthority(role);
            authority = authorityRepository.save(authority);
            user.getAuthorities().add(authority);
        }

        return userRepository.save(user);
    }

}
