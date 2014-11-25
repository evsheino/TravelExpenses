package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wad.auth.ForcePasswordChangeAuthenticationSuccessHandler;
import wad.domain.User;
import wad.repository.UserRepository;
import wad.service.UserService;

import javax.servlet.http.HttpServletRequest;

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
    public String forcePasswordChange(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        return "forcechangepassword";
    }

    @RequestMapping(value = "/password/save", method = RequestMethod.POST)
    public String changePassword(HttpServletRequest request, @RequestParam String newPassword) {
        User user = userService.getCurrentUser();

        user.setPassword(newPassword);
        user.setPasswordExpired(Boolean.FALSE);
        userRepository.save(user);

        request.getSession().setAttribute(ForcePasswordChangeAuthenticationSuccessHandler.SESSION_KEY_FORCE_PASSWORD_CHANGE, Boolean.FALSE);

        return "redirect:/index";
    }

}
