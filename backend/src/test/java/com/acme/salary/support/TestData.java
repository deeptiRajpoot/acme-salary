package com.acme.salary.support;

import com.acme.salary.employee.EmployeeInput;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestData {

    private TestData() {
    }

    public static EmployeeInput input(String email) {
        return new EmployeeInput("Ada", "Lovelace", email, "US", "Engineering", "L3", "Software Engineer", "F",
                LocalDate.of(2020, 1, 15), new BigDecimal("100000"), "USD");
    }

    public static EmployeeInput input(String email, String last, String country, String dept, String level,
                                      String gender, String salary, String currency) {
        return new EmployeeInput("Pat", last, email, country, dept, level, "Engineer", gender,
                LocalDate.of(2020, 1, 15), new BigDecimal(salary), currency);
    }
}
