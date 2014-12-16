package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wad.domain.Authority;
import wad.domain.User;
import wad.repository.UserRepository;
import wad.service.UserService;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("user")
    private User getUser() {
        return new User();
    }

    @RequestMapping(value="/users", method = RequestMethod.GET)
    public String viewAll(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/admin";
    }

    @RequestMapping(value="/user/new", method = RequestMethod.GET)
    public String newUser() {
        return "admin/edituser";
    }

    @RequestMapping(value="/user/new/save", method = RequestMethod.POST)
    public String saveNewUser(@RequestParam String name, @RequestParam String username, @RequestParam String[] roles) {
        Authority.Role[] userRoles = new Authority.Role[roles.length];
        for(int i = 0; i < roles.length; i++) {
            userRoles[i] = Authority.Role.valueOf(roles[i]);
        }

        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setPassword(username);
        user.setPasswordExpired(true);
        userService.saveUser(user, userRoles);

        return "redirect:/admin/users";
    }

    @RequestMapping(value="/user/{id}", method = RequestMethod.GET)
    public String editUser(Model model, @PathVariable Long id) {
        model.addAttribute("user", userRepository.findOne(id));
        return "admin/edituser";
    }

    @RequestMapping(value="/user/{id}/resetpassword", method = RequestMethod.GET)
    public String resetPassword(Model model, @PathVariable Long id) {
        User user = userRepository.findOne(id);
        //UUID uuid = UUID.randomUUID();

        String uuid = new BigInteger(130, new SecureRandom()).toString(32);

        user.setPassword(uuid);
        user.setPasswordExpired(Boolean.TRUE);
        userRepository.save(user);

        model.addAttribute("uuid", uuid);
        model.addAttribute("user", user);
        // Create password save it to user and set force change password

        return "/admin/resetpassword";
    }

    @RequestMapping(value="/user/{id}/save", method = RequestMethod.POST)
    public String saveUser(@PathVariable Long id, @RequestParam String name, @RequestParam String username,  @RequestParam(defaultValue = "false") Boolean forcePasswordChange, @RequestParam String[] roles) {
        Authority.Role[] userRoles = new Authority.Role[roles.length];
        for(int i = 0; i < roles.length; i++) {
            userRoles[i] = Authority.Role.valueOf(roles[i]);
        }
        User user = userRepository.findOne(id);
        user.setName(user.getName());
        user.setUsername(user.getUsername());
        user.setPasswordExpired(forcePasswordChange);
        userService.saveUser(user, userRoles);
        return "redirect:/admin/users";
    }

}
