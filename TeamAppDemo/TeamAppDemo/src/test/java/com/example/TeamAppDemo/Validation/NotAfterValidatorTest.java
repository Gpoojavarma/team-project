
package com.example.TeamAppDemo.Validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NotAfterValidatorTest {

    static class Dummy {
        @NotAfter("2025-12-31")
        LocalDate dateLimit_2025_12_31;

        @NotAfter("2024-01-01")
        LocalDate dateLimit_2024_01_01;
    }

    private NotAfter getAnnotationFromField(String name) throws NoSuchFieldException {
        Field f = Dummy.class.getDeclaredField(name);
        return f.getAnnotation(NotAfter.class);
    }

    @Test
    @DisplayName("initialize parses date and validates correctly")
    void initialize_and_validate() throws Exception {
        NotAfterValidator validator = new NotAfterValidator();
        validator.initialize(getAnnotationFromField("dateLimit_2025_12_31"));

        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(LocalDate.of(2025, 12, 30), ctx));
        assertTrue(validator.isValid(LocalDate.of(2025, 12, 31), ctx));
        assertFalse(validator.isValid(LocalDate.of(2026, 1, 1), ctx));
    }

    @Test
    @DisplayName("null is valid (Bean Validation convention)")
    void null_isValid() throws Exception {
        NotAfterValidator validator = new NotAfterValidator();
        validator.initialize(getAnnotationFromField("dateLimit_2024_01_01"));

        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

        assertTrue(validator.isValid(null, ctx));
    }
}
