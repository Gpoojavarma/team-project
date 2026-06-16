package com.example.TeamAppDemo.Exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/throw")
@Validated
class ThrowingController {

    // 1) ResourceNotFoundException
    @GetMapping("/not-found")
    public String notFound() {
        throw new ResourceNotFoundException("race not found");
    }

    // 2) BusinessRuleViolationException
    @GetMapping("/business")
    public String business() {
        throw new BusinessRuleViolationException("rule broken");
    }

    // 3) IllegalArgumentException
    @GetMapping("/illegal")
    public String illegal() {
        throw new IllegalArgumentException("illegal arg");
    }

    // 4) NumberFormatException
    @GetMapping("/number")
    public String number() {
        throw new NumberFormatException("for input string: abc");
    }

    // 5) DataIntegrityViolationException
    @GetMapping("/integrity")
    public String integrity() {
        throw new DataIntegrityViolationException("duplicate");
    }

    // 6) Generic Exception
    @GetMapping("/generic")
    public String generic() throws Exception {
        throw new Exception("some internal");
    }

    // 7) ConstraintViolationException
    @GetMapping("/constraint")
    public String constraint(@RequestParam @Min(10) int value) {
        return "ok";
    }

    // 8) MethodArgumentNotValidException
    static class SampleDTO {
        @NotBlank(message = "name required")
        public String name;
    }

    @PostMapping(value = "/invalid-body", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String invalidBody(@Valid @RequestBody SampleDTO dto) {
        return "ok";
    }

    // 9) HttpMessageNotReadableException
    @PostMapping(value = "/malformed", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String malformed(@RequestBody SampleDTO dto) {
        return "ok";
    }
}