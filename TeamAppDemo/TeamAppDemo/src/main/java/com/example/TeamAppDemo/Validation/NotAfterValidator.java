
package com.example.TeamAppDemo.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class NotAfterValidator implements ConstraintValidator<NotAfter, LocalDate> {
    private LocalDate limit;

    @Override
    public void initialize(NotAfter constraintAnnotation) {
        this.limit = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return !value.isAfter(limit);
    }
}
