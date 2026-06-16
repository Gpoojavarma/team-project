
package com.example.TeamAppDemo.DTO;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DriverDtosTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private DriverDtos.DriverCreateRequest validCreate() {
        DriverDtos.DriverCreateRequest dto = new DriverDtos.DriverCreateRequest();
        dto.firstName = "Lewis";
        dto.lastName = "Hamilton";
        dto.dateOfBirth = LocalDate.of(1985, 1, 7); // before 2000-12-31
        dto.teamId = 1L; // optional
        return dto;
    }

    @Test
    void createRequest_valid_passes() {
        var dto = validCreate();
        Set<ConstraintViolation<DriverDtos.DriverCreateRequest>> v = validator.validate(dto);
        assertTrue(v.isEmpty(), "Expected no violations for a valid create request");
    }

    @Test
    void createRequest_blankNames_failNotBlank() {
        var dto = validCreate();
        dto.firstName = " ";
        dto.lastName = "";
        var v = validator.validate(dto);
        assertTrue(hasViolation(v, "firstName"), "firstName should violate @NotBlank");
        assertTrue(hasViolation(v, "lastName"), "lastName should violate @NotBlank");
    }

    @Test
    void createRequest_nameTooLong_failsSize() {
        var dto = validCreate();
        dto.firstName = "A".repeat(97);
        dto.lastName  = "B".repeat(97);
        var v = validator.validate(dto);
        assertTrue(hasViolation(v, "firstName") || hasViolation(v, "lastName"),
            "Names longer than 96 should violate @Size(max=96)");
    }

    @Test
    void createRequest_nullDob_failsNotNull() {
        var dto = validCreate();
        dto.dateOfBirth = null;
        var v = validator.validate(dto);
        assertTrue(hasViolation(v, "dateOfBirth"), "dateOfBirth should violate @NotNull");
    }

    @Test
    void createRequest_dobAfterCutoff_failsNotAfter() {
        var dto = validCreate();
        dto.dateOfBirth = LocalDate.of(2001, 1, 1); // after 2000-12-31
        var v = validator.validate(dto);
        assertTrue(hasViolation(v, "dateOfBirth"),
            "dateOfBirth after 2000-12-31 should violate @NotAfter(\"2000-12-31\")");
    }

    @Test
    void updateRequest_sameConstraintsAsCreate() {
        DriverDtos.DriverUpdateRequest dto = new DriverDtos.DriverUpdateRequest();
        dto.firstName = "  "; // NotBlank violation
        dto.lastName = "Max";
        dto.dateOfBirth = LocalDate.of(2020, 1, 1); // violates @NotAfter("2000-12-31")
        dto.teamId = null; // allowed

        var v = validator.validate(dto);
        assertTrue(hasViolation(v, "firstName"), "firstName should violate @NotBlank");
        assertTrue(hasViolation(v, "dateOfBirth"), "dateOfBirth should violate @NotAfter");
    }

    // ---- helper ----
    private static <T> boolean hasViolation(Set<ConstraintViolation<T>> v, String property) {
        return v.stream().anyMatch(cv -> property.equals(cv.getPropertyPath().toString()));
    }
}
