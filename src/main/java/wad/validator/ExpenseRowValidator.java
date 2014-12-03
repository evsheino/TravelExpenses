package wad.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import wad.domain.Expense;
import wad.domain.ExpenseRow;

@Component
public class ExpenseRowValidator implements Validator {

    @Autowired
    @Qualifier("basicValidator")
    Validator basicValidator;

    @Override
    public boolean supports(Class clazz) {
        return ExpenseRow.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        ExpenseRow row = (ExpenseRow) obj;

        ValidationUtils.invokeValidator(basicValidator, row, e);

        Expense expense = row.getExpense();
        if (row.getDate() != null && expense != null) {
            if (expense.getStartDate() != null) {
                if (row.getDate().before(row.getExpense().getStartDate()))
                    e.rejectValue("date", "invalidvalue", "Date should not be earlier than the start date of the expense.");
            }
            if (expense.getEndDate() != null) {
                if (row.getDate().after(row.getExpense().getEndDate()))
                    e.rejectValue("date", "invalidvalue", "Date should not be later than the end date of the expense.");
            }
        }
    }
}
