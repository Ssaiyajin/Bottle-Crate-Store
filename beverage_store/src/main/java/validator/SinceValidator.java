package beverage_store.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class SinceValidator implements ConstraintValidator<Since, LocalDate> {

    private LocalDate threshold;

    @Override
    public void initialize(Since constraintAnnotation) {
        String value = constraintAnnotation.value();
        if (value == null || value.isBlank()) {
            this.threshold = LocalDate.MIN;
            return;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            this.threshold = LocalDate.parse(value, dtf);
        } catch (DateTimeParseException e) {
            this.threshold = LocalDate.MIN;
        }
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        // allow null — let @NotNull handle presence if required
        if (value == null) {
            return true;
        }
        return value.isAfter(threshold);
    }
}
