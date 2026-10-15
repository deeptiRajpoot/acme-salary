package com.acme.salary.analytics;

/** Minimal projection of an employee used by analytics. */
public record PayRow(long id, String name, String country, String department, String level, String gender,
                     double salaryUsd) {
}
