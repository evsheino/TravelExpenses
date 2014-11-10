package wad.profiles;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import wad.domain.Authority;
import wad.domain.Expense;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.UserRepository;
import wad.service.UserService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nryytty@cs on 7.11.2014.
 */
public abstract class BaseProfile {

    public abstract TemplateResolver getTemplateResolver();

    public abstract String getTemplatePath();

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        User johnd = userService.createUser("John Doe", "johnd", "johnd", Authority.Role.USER);
        User foob =userService.createUser("Foo Bar", "foob", "foob", Authority.Role.USER, Authority.Role.SUPERVISOR);
        userService.createUser("Clint Eastwood", "clinte", "clinte", Authority.Role.USER, Authority.Role.SUPERVISOR, Authority.Role.ADMIN);
    }

    private User generateExpenses(User user, int numOfExpenses) {
        user.setExpenses(new ArrayList<Expense>());

        for(int i = 0; i < numOfExpenses; i++) {
            Expense e = new Expense();
            e.setDate(new Date());
            e.setUser(user);
            e.setAmount((i^2) + 11.99 + 1);
            e = expenseRepository.save(e);
            user.getExpenses().add(e);
        }
        return userRepository.save(user);

    }


    @Bean
    public ITemplateResolver templateResolver() {
        TemplateResolver templateResolver = getTemplateResolver();
        templateResolver.setPrefix(getTemplatePath());
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);

        return templateResolver;
    }


    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new SpringSecurityDialect());

        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver(){
        ThymeleafViewResolver viewResolver= new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());

        return viewResolver;
    }

}
