package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import wad.domain.Authority;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.AuthorityRepository;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;

import java.util.ArrayList;
import java.util.Iterator;

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

    public User createUser(User user, Authority.Role... roles) {
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

    public User createUser(String name, String username, String password, Authority.Role... roles) {
        User user = new User(name, username, password);
        return createUser(user, roles);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username);
        userRepository.delete(user);
    }

}
