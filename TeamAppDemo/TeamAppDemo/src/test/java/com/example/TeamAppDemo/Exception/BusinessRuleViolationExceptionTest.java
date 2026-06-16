package com.example.TeamAppDemo.Exception;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusinessRuleViolationExceptionTest {

    @Test
    void message_is_propagated() {
        BusinessRuleViolationException ex = new BusinessRuleViolationException("rule violated");
        assertEquals("rule violated", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }
}
