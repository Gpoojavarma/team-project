package com.example.TeamAppDemo.Exception;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void message_is_propagated() {
        ResourceNotFoundException ex = new ResourceNotFoundException("missing resource");
        assertEquals("missing resource", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }
}

