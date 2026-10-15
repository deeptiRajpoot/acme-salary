package com.acme.salary.employee;

import java.util.Set;

/** Snapshot of valid lookup values, loaded once per request/import. */
public record ReferenceData(Set<String> countries, Set<String> departments, Set<String> levels,
                            Set<String> currencies) {
}
