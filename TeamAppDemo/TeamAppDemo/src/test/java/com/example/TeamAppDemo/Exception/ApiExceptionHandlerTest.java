package com.example.TeamAppDemo.Exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;

@WebMvcTest(controllers = ThrowingController.class)
@Import(ApiExceptionHandler.class)
class ApiExceptionHandlerTest {

    @Resource
    private MockMvc mvc;

    @Resource
    private ObjectMapper om;

    // 1) ResourceNotFoundException
    @Test
    void not_found() throws Exception {
        mvc.perform(get("/throw/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("race not found"));
    }

    // 2) BusinessRuleViolationException
    @Test
    void business_rule() throws Exception {
        mvc.perform(get("/throw/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("rule broken"));
    }

    // 3) IllegalArgumentException
    @Test
    void illegal_argument() throws Exception {
        mvc.perform(get("/throw/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("illegal arg"));
    }

    // 4) NumberFormatException
    @Test
    void number_format() throws Exception {
        mvc.perform(get("/throw/number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", startsWith("Invalid numeric id: ")));
    }

    // 5) HttpMessageNotReadableException
    @Test
    void malformed_json() throws Exception {
        mvc.perform(post("/throw/malformed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON or wrong types"));
    }

    // 6) MethodArgumentNotValidException
    @Test
    void invalid_body() throws Exception {
        mvc.perform(post("/throw/invalid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]", containsString("name required")));
    }

    // 7) ConstraintViolationException
    @Test
    void constraint_violation() throws Exception {
        mvc.perform(get("/throw/constraint").param("value", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("value")));
    }

    // 8) DataIntegrityViolationException
    @Test
    void data_integrity() throws Exception {
        mvc.perform(get("/throw/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Driver already registered to this race"));
    }

    // 9) Generic Exception
    @Test
    void generic_exception() throws Exception {
        mvc.perform(get("/throw/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal error"));
    }
}