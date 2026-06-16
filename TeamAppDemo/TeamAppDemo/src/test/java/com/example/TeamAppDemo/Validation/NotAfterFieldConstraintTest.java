package com.example.TeamAppDemo.Validation;
import com.example.TeamAppDemo.Validation.NotAfter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotAfterFieldConstraintTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /** DTO that uses the existing field-level @NotAfter("yyyy-MM-dd") */
    static class CutoffDto {

        // Your existing annotation expects a fixed date string limit
        @NotAfter("2026-12-31")
        private LocalDate date;

        CutoffDto(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() { return date; }
    }

    @Test
    @DisplayName("Valid: date <= limit")
    void dateBeforeOrEqualLimit_isValid() {
        CutoffDto eq = new CutoffDto(LocalDate.of(2026, 12, 31));
        CutoffDto lt = new CutoffDto(LocalDate.of(2026, 12, 30));

        assertTrue(validator.validate(eq).isEmpty(), "date == limit should be valid");
        assertTrue(validator.validate(lt).isEmpty(), "date < limit should be valid");
    }

    @Test
    @DisplayName("Invalid: date > limit")
    void dateAfterLimit_isInvalid() {
        CutoffDto gt = new CutoffDto(LocalDate.of(2027, 1, 1));
        Set<ConstraintViolation<CutoffDto>> violations = validator.validate(gt);
        assertFalse(violations.isEmpty(), "date > limit should be invalid");
        // Optionally assert message if using the default template:
        // String msg = violations.iterator().next().getMessage();
        // assertTrue(msg.contains("on or before"));
    }

    @Test
    @DisplayName("Null -> valid (use @NotNull to enforce non-null)")
    void nullDate_isValidByThisConstraint() {
        CutoffDto nullDto = new CutoffDto(null);
        assertTrue(validator.validate(nullDto).isEmpty(), "Nulls should be valid unless @NotNull is used");
    }
}

