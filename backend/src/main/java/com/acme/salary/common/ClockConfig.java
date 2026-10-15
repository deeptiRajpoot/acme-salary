package com.acme.salary.common;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Fixed instant (ISO-8601) makes time-dependent behaviour deterministic, e.g. in tests. */
    @Bean
    public Clock clock(@Value("${acme.clock.fixed:}") String fixed) {
        return fixed.isBlank() ? Clock.systemUTC() : Clock.fixed(Instant.parse(fixed), ZoneOffset.UTC);
    }
}
