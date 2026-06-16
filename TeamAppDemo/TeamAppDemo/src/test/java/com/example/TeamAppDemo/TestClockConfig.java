package com.example.TeamAppDemo;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {
    @Bean
    @Primary
    public Clock fixedTestClock() {
        return Clock.fixed(Instant.parse("2025-01-15T00:00:00Z"), ZoneOffset.UTC);
    }
}