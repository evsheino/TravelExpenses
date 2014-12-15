package wad.validator;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import wad.domain.Receipt;

@Component
public class ReceiptValidator implements Validator {
    
    @Autowired
    @Qualifier("basicValidator")
    Validator basicValidator;

    @Override
    public boolean supports(Class clazz) {
        return Receipt.class.isAssignableFrom(clazz);
    }
    
    @Override
    public void validate(Object obj, Errors e) {
        Receipt receipt = (Receipt) obj;

        ValidationUtils.invokeValidator(basicValidator, receipt, e);

        if (!Receipt.allowedContentTypes.contains(receipt.getMediaType()))
            e.rejectValue("mediaType", "invalidvalue", "Only the following media types are allowed: " + Arrays.toString(Receipt.allowedContentTypes.toArray()));
    }
}
