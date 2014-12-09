package wad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wad.domain.Expense;
import wad.service.ExpenseService;

@Controller
@RequestMapping("*")
public class DefaultController {

    private static final int EXPENSE_LIST_SIZE = 5;

    @Autowired
    private ExpenseService expenseService;

    @RequestMapping(method = RequestMethod.GET)
    public String viewIndex(Model model, @RequestParam(required = false) Expense.Status status, @RequestParam(required = false) Integer pageNumber) {
        model.addAttribute("drafts", expenseService.getPagedExpenses(Expense.Status.DRAFT, ExpenseService.FIRST_PAGE, EXPENSE_LIST_SIZE).getContent());
        model.addAttribute("sent", expenseService.getPagedExpenses(Expense.Status.SENT, ExpenseService.FIRST_PAGE, EXPENSE_LIST_SIZE).getContent());
        model.addAttribute("rejected", expenseService.getPagedExpenses(Expense.Status.REJECTED, ExpenseService.FIRST_PAGE, EXPENSE_LIST_SIZE).getContent());
        model.addAttribute("approved", expenseService.getPagedExpenses(Expense.Status.APPROVED, ExpenseService.FIRST_PAGE, EXPENSE_LIST_SIZE).getContent());

        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String viewLogin(Model model) {
        return "login";
    }
}
