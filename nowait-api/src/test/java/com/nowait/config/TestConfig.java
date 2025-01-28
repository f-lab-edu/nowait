package com.nowait.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    public Clock clock() {
        return Clock.fixed(
            LocalDateTime.of(2024, 11, 1, 0, 5, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant(), ZoneId.systemDefault()
        );
    }
}
