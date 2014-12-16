package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import wad.domain.Authority;
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.ExpenseRepository;
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

    @Autowired
    private ExpenseRepository expenseRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user, Authority.Role... roles) {
        // Assign empty list for user and save
        user.setAuthorities(new ArrayList<Authority>());
        user = userRepository.save(user);
        // Delete old authorities
        authorityRepository.deleteAuthoritiesByUser(user);
        //Assign new authorities
        if(roles != null) {
            for(Authority.Role role : roles) {
                user.getAuthorities().add(new Authority(user, role));
            }
        }
        return userRepository.save(user);
    }

    public User createUser(String name, String username, String password, Authority.Role... roles) {
        return this.createUser(name, username, password, false, roles);
    }

    public User createUser(String name, String username, String password, boolean passwordExpired, Authority.Role... roles) {
        User user = new User(name, username, password);
        user.setPasswordExpired(passwordExpired);
        return this.saveUser(user, roles);
    }
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username);
        userRepository.delete(user);
    }

}
