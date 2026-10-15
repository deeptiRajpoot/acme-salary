package com.acme.salary.importexport;

import java.util.List;

/** CSV layout shared by import and export (export output can be re-imported). */
final class CsvColumns {

    static final List<String> HEADER = List.of("firstName", "lastName", "email", "country", "department", "level",
            "jobTitle", "gender", "hireDate", "salary", "currency");

    private CsvColumns() {
    }
}
