package wad.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import wad.domain.Expense;

public class ExpenseValidator implements Validator {

    @Override
    public boolean supports(Class clazz) {
        return Expense.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        Expense expense = (Expense) obj;
        if (expense.getStartDate().after(expense.getEndDate()))
            e.rejectValue("endDate", "invalidvalue", "End date should not be earlier than start date.");
    }
}
