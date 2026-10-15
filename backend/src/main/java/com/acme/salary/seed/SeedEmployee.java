package com.acme.salary.seed;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SeedEmployee(String firstName, String lastName, String email, String country, String department,
                           String level, String jobTitle, String gender, LocalDate hireDate,
                           BigDecimal salary, String currency, BigDecimal salaryUsd) {
}
