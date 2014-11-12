package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import wad.domain.Authority;
import wad.domain.User;
import wad.repository.UserRepository;
import wad.service.UserService;

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

    @RequestMapping(value="/user/save", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute("user") User user, @RequestParam String[] roles) {
        Authority.Role[] userRoles = new Authority.Role[roles.length];
        for(int i = 0; i < roles.length; i++) {
            userRoles[i] = Authority.Role.valueOf(roles[i]);
        }
        userService.createUser(user, userRoles);
        return "redirect:/admin/users";
    }

    @RequestMapping(value="/user/new", method = RequestMethod.GET)
    public String newUser() {
        return "admin/edituser";
    }

    @RequestMapping(value="/user/{id}", method = RequestMethod.GET)
    public String editUser(Model model, @PathVariable Long id) {
        model.addAttribute("user", userRepository.findOne(id));
        return "admin/edituser";
    }

}
