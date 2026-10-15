package com.acme.salary.importexport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.common.ValidationException;
import com.acme.salary.employee.EmployeeFilter;
import com.acme.salary.employee.EmployeeService;
import com.acme.salary.support.IntegrationTest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ImportExportTest {

    private static final String HEADER = "firstName,lastName,email,country,department,level,jobTitle,gender,hireDate,salary,currency\n";

    @Autowired
    ImportService importService;
    @Autowired
    ExportService exportService;
    @Autowired
    EmployeeService employees;

    private ImportResult run(String csv) {
        return importService.importCsv(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void goodRowsAreSavedWithHistory() {
        ImportResult r = run(HEADER
                + "Ann,Lee,ann@x.com,US,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n"
                + "Bo,\"Smith, Jr\",bo@x.com,GB,Sales,L2,AE,M,2022-05-09,60000,GBP\n");
        assertThat(r.totalRows()).isEqualTo(2);
        assertThat(r.imported()).isEqualTo(2);
        assertThat(r.failed()).isZero();
        var page = employees.search(new EmployeeFilter("bo@x", null, null, null), 0, 10, "id,asc");
        assertThat(page.content()).singleElement().satisfies(e -> {
            assertThat(e.lastName()).isEqualTo("Smith, Jr");
            assertThat(e.salaryUsd()).isEqualByComparingTo("76200.00");
            assertThat(employees.salaryHistory(e.id())).hasSize(1);
        });
    }

    @Test
    void badRowsAreReportedWithRowNumberAndGoodRowsStillSaved() {
        ImportResult r = run(HEADER
                + "Ann,Lee,ann@x.com,US,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n"   // row 2 ok
                + "Bad,Email,nope,US,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n"      // row 3
                + "Bad,Country,c@x.com,ZZ,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n" // row 4
                + "Bad,Date,d@x.com,US,Engineering,L3,Engineer,F,03/01/2021,120000,USD\n"    // row 5
                + "Bad,Salary,e@x.com,US,Engineering,L3,Engineer,F,2021-03-01,abc,USD\n"     // row 6
                + "Dup,Lee,ANN@x.com,US,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n"   // row 7
                + "Short,Row,f@x.com\n");                                                     // row 8
        assertThat(r.totalRows()).isEqualTo(7);
        assertThat(r.imported()).isEqualTo(1);
        assertThat(r.failed()).isEqualTo(6);
        assertThat(r.errors()).extracting(ImportResult.RowError::row).containsExactly(3, 4, 5, 6, 7, 8);
        assertThat(r.errors().get(0).message()).contains("email");
        assertThat(r.errors().get(1).message()).contains("country");
        assertThat(r.errors().get(2).message()).contains("hireDate");
        assertThat(r.errors().get(3).message()).contains("salary");
        assertThat(r.errors().get(4).message()).contains("already in use");
    }

    @Test
    void badHeaderIsRejectedAndNothingSaved() {
        assertThatThrownBy(() -> run("first,last,mail\nA,B,c@x.com\n")).isInstanceOfSatisfying(ValidationException.class,
                ex -> assertThat(ex.getErrors().get("header")).contains("missing columns"));
    }

    @Test
    void bomAndHeaderCaseAreTolerated() {
        ImportResult r = run("﻿" + HEADER.toUpperCase().replace("\n", "\n").toLowerCase()
                + "Ann,Lee,bom@x.com,US,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n");
        assertThat(r.imported()).isEqualTo(1);
    }

    @Test
    void exportRespectsFiltersAndRoundTrips() throws IOException {
        run(HEADER
                + "Ann,Lee,ann@x.com,US,Engineering,L3,Engineer,F,2021-03-01,120000,USD\n"
                + "Bo,Kim,bo@x.com,GB,Sales,L2,AE,M,2022-05-09,60000.50,GBP\n"
                + "Cy,=cmd,cy@x.com,GB,Sales,L2,AE,X,2022-05-09,1,GBP\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exportService.write(new EmployeeFilter(null, "GB", null, null), out);
        String csv = out.toString(StandardCharsets.UTF_8);
        assertThat(csv.lines().toList()).hasSize(3);
        assertThat(csv).startsWith(HEADER.trim());
        assertThat(csv).contains("bo@x.com").doesNotContain("ann@x.com").contains("60000.5").contains("'=cmd");
    }
}
