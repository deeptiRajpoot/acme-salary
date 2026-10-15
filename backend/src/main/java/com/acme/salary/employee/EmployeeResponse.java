package com.acme.salary.employee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record EmployeeResponse(Long id, String firstName, String lastName, String email, String country,
                               String department, String level, String jobTitle, String gender,
                               LocalDate hireDate, BigDecimal salary, String currency, BigDecimal salaryUsd) {

    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getCountry(),
                e.getDepartment(), e.getLevel(), e.getJobTitle(), e.getGender(), e.getHireDate(),
                e.getSalary().setScale(2, RoundingMode.HALF_UP), e.getCurrency(),
                e.getSalaryUsd().setScale(2, RoundingMode.HALF_UP));
    }
}
