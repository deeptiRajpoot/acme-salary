package com.acme.salary.employee;

/** Search/export filters; blank values mean "no filter". */
public record EmployeeFilter(String q, String country, String department, String level) {
}
