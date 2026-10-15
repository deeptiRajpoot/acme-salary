package com.acme.salary.salary;

import com.acme.salary.common.LookupRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FxConfig {

    /** Rates are static reference data, so they are loaded once at startup. */
    @Bean
    public FxConverter fxConverter(LookupRepository lookups) {
        return new FxConverter(lookups.currencyRates());
    }
}
