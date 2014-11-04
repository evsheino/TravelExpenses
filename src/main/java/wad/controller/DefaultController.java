package wad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("*")
public class DefaultController {

    @RequestMapping(method = RequestMethod.GET)
    public String viewIndex(Model model) {
        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String viewLogin(Model model) {
        return "login";
    }
}
