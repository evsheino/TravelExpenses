package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wad.domain.User;
import wad.repository.UserRepository;
import wad.service.UserService;

@Controller
@RequestMapping("/account")
public class AccountSettingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String viewSettings(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        return "accountSettings";
    }

    @RequestMapping(value="/save", method = RequestMethod.POST)
    public String saveSettings(@RequestParam String oldPassword, @RequestParam String password) {
        User user  = userService.getCurrentUser();
        user.setPassword(password);
        userRepository.save(user);
        return "redirect:/account";
    }

    @RequestMapping(value = "/password/force", method = RequestMethod.GET)
    public String forcePasswordChange() {
        return "changepassword";
    }

    @RequestMapping(value = "/password", method = RequestMethod.GET)
    public String changePassword() {
        return "changepassword";
    }

    @RequestMapping(value = "/password/save", method = RequestMethod.POST)
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        return "redirect:/index";
    }

}
