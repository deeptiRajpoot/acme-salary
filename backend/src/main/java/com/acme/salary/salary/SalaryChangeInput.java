package com.acme.salary.salary;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SalaryChangeInput(
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal newSalary,
        @NotBlank @Size(min = 3, max = 500) String reason,
        @Size(max = 100) String changedBy) {

    public static final String DEFAULT_CHANGED_BY = "hr.manager";

    public String changedByOrDefault() {
        return changedBy == null || changedBy.isBlank() ? DEFAULT_CHANGED_BY : changedBy.trim();
    }
}
