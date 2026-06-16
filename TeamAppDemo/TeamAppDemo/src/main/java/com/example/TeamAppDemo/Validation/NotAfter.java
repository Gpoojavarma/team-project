
package com.example.TeamAppDemo.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotAfterValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAfter {
    String message() default "must be on or before {value}";
    String value();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
