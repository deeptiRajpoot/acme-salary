package com.acme.salary.common;

import java.util.List;

public record Lookups(List<CountryInfo> countries, List<String> departments, List<String> levels,
                      List<String> currencies) {

    public record CountryInfo(String code, String name, String currency) {
    }
}
