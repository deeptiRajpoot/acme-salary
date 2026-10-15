package com.acme.salary.employee;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeInput(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank String country,
        @NotBlank String department,
        @NotBlank String level,
        @NotBlank @Size(max = 120) String jobTitle,
        @NotBlank @Pattern(regexp = "[FMX]", message = "must be one of F, M, X") String gender,
        @NotNull LocalDate hireDate,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal salary,
        @NotBlank String currency) {

    /** Trims text and upper/lower-cases codes so later lookups and uniqueness checks are exact. */
    public EmployeeInput normalized() {
        return new EmployeeInput(trim(firstName), trim(lastName), lower(email), upper(country), trim(department),
                upper(level), trim(jobTitle), upper(gender), hireDate, salary, upper(currency));
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    private static String lower(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }
}
