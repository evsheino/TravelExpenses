package wad.validator;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import wad.domain.Expense;
import wad.domain.ExpenseRow;

@Component
public class ExpenseValidator implements Validator {
    
    @Autowired
    @Qualifier("expenseRowValidator")
    ExpenseRowValidator rowValidator;

    @Autowired
    @Qualifier("basicValidator")
    Validator basicValidator;

    @Override
    public boolean supports(Class clazz) {
        return Expense.class.isAssignableFrom(clazz);
    }
    
    @Override
    public void validate(Object obj, Errors e) {
        Expense expense = (Expense) obj;

        ValidationUtils.invokeValidator(basicValidator, expense, e);

        if (expense.getStartDate() != null && expense.getEndDate() != null) {
            if (expense.getStartDate().after(expense.getEndDate()))
                e.rejectValue("endDate", "invalidvalue", "End date should not be earlier than start date.");
        }
        List<ExpenseRow> rowList = expense.getExpenseRows();
        
        if (rowList != null) {
            int rowCount = rowList.size();
            for (int i=0; i < rowCount; i++) {
                try {
                    e.pushNestedPath("expenseRows[" + i + "]");
                    ExpenseRow row = rowList.get(i);
                    ValidationUtils.invokeValidator(rowValidator, row, e);
                } finally {
                    e.popNestedPath();
                }
            }
        }
    }
}
