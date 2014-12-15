package wad.profiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import wad.domain.Authority;
import wad.domain.Expense;
import wad.domain.ExpenseRow;
import wad.domain.User;
import wad.repository.ExpenseRepository;
import wad.repository.ExpenseRowRepository;
import wad.repository.UserRepository;
import wad.service.ExpenseService;
import wad.service.UserService;

@Configuration
@Profile(value = {"dev", "default"})
public class DevProfile extends BaseProfile {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseRowRepository expenseRowRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        userService.createUser("Clint Eastwood", "clinte", "clinte", Authority.Role.ROLE_USER, Authority.Role.ROLE_SUPERVISOR, Authority.Role.ROLE_ADMIN);
        User foob = userService.createUser("Foo Bar", "foob", "foob", Authority.Role.ROLE_USER, Authority.Role.ROLE_SUPERVISOR);
        generateExpenses(foob, 9, Expense.Status.DRAFT);
        generateExpenses(foob, 13, Expense.Status.SENT);

        User johnd = userService.createUser("John Doe", "johnd", "johnd", true, Authority.Role.ROLE_USER);
        generateExpenses(johnd, 28, Expense.Status.DRAFT);
        generateExpenses(johnd, 9, Expense.Status.SENT);

        userService.createUser("Teemu", "Teemu", "sisaan", Authority.Role.ROLE_USER);
        userService.createUser("Aku", "Aku", "password", Authority.Role.ROLE_ADMIN);
        userService.createUser("Tommi", "Tommi", "esimies", Authority.Role.ROLE_SUPERVISOR);
    }

    private void generateExpenses(User user, int numOfExpenses, Expense.Status status) {
        user.setExpenses(new ArrayList<Expense>());
        for(int i = 0; i < numOfExpenses; i++) {
            Expense e = expenseService.createExpense(user, new Date(), new Date(), (i^2), user.getName() + " blaab", status);
            generateExpenseRows(e, i);
        }
    }

    private void generateExpenseRows(Expense expense, int numOfRows) {
        expense.setExpenseRows(new ArrayList<ExpenseRow>());
        for(int i = 0; i < numOfRows; i++) {
            ExpenseRow row = new ExpenseRow(expense, new BigDecimal(i).add(new BigDecimal("0.99")), "Unavoidable expense like lunch with frieds "+ 1, new Date() );
            expenseRowRepository.save(row);
        }
    }

    @Override
    public String getTemplatePath() {
        return "/WEB-INF/templates/";
    }

    @Override
    public TemplateResolver getTemplateResolver() {
        return new ServletContextTemplateResolver();
    }

}

