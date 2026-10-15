package com.acme.salary.common;

import java.util.ArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final LookupRepository lookups;

    public LookupController(LookupRepository lookups) {
        this.lookups = lookups;
    }

    @GetMapping
    public Lookups get() {
        return new Lookups(lookups.countries(), lookups.departments(), lookups.levels(),
                new ArrayList<>(lookups.currencyRates().keySet()));
    }
}
