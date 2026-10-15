package com.acme.salary.importexport;

import com.acme.salary.common.ValidationException;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeInput;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmployeeValidator;
import com.acme.salary.employee.ReferenceData;
import com.acme.salary.salary.FxConverter;
import com.acme.salary.salary.SalaryChangeInput;
import com.acme.salary.salary.SalaryHistoryService;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** CSV import: valid rows are saved (one transaction), invalid rows are reported with their row number. */
@Service
public class ImportService {

    private static final String REASON = "Imported from CSV";

    private final EmployeeRepository employees;
    private final EmployeeValidator validator;
    private final FxConverter fx;
    private final SalaryHistoryService history;
    private final TransactionTemplate tx;

    public ImportService(EmployeeRepository employees, EmployeeValidator validator, FxConverter fx,
                         SalaryHistoryService history, TransactionTemplate tx) {
        this.employees = employees;
        this.validator = validator;
        this.fx = fx;
        this.history = history;
        this.tx = tx;
    }

    public ImportResult importCsv(InputStream in) {
        List<ImportResult.RowError> errors = new ArrayList<>();
        List<EmployeeInput> valid = new ArrayList<>();
        int total = parse(in, errors, valid);
        tx.executeWithoutResult(status -> valid.forEach(this::persist));
        return new ImportResult(total, valid.size(), errors.size(), errors);
    }

    private int parse(InputStream in, List<ImportResult.RowError> errors, List<EmployeeInput> valid) {
        ReferenceData ref = validator.referenceData();
        Set<String> taken = new HashSet<>(employees.findAllEmails());
        int total = 0;
        try (CSVParser parser = open(in)) {
            checkHeader(parser.getHeaderMap().keySet());
            for (CSVRecord rec : parser) {
                total++;
                int row = (int) rec.getRecordNumber() + 1; // header is row 1
                String problem = rowProblem(rec, ref, taken, valid);
                if (problem != null) {
                    errors.add(new ImportResult.RowError(row, problem));
                }
            }
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof ValidationException ve) {
                throw ve;
            }
            throw new ValidationException("file", "Malformed CSV: " + ex.getMessage());
        }
        return total;
    }

    /** @return error message, or null when the row was accepted into {@code valid} */
    private String rowProblem(CSVRecord rec, ReferenceData ref, Set<String> taken, List<EmployeeInput> valid) {
        if (!rec.isConsistent()) {
            return "expected " + CsvColumns.HEADER.size() + " columns but found " + rec.size();
        }
        Map<String, String> errors = new LinkedHashMap<>();
        EmployeeInput input = toInput(rec, errors).normalized();
        errors.putAll(validator.validate(input, ref, taken::contains));
        if (!errors.isEmpty()) {
            return errors.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("; "));
        }
        taken.add(input.email());
        valid.add(input);
        return null;
    }

    private EmployeeInput toInput(CSVRecord r, Map<String, String> errors) {
        LocalDate hireDate = null;
        BigDecimal salary = null;
        try {
            hireDate = r.get("hireDate").isBlank() ? null : LocalDate.parse(r.get("hireDate").trim());
        } catch (DateTimeParseException ex) {
            errors.put("hireDate", "must be an ISO date (yyyy-MM-dd)");
        }
        try {
            salary = r.get("salary").isBlank() ? null : new BigDecimal(r.get("salary").trim());
        } catch (NumberFormatException ex) {
            errors.put("salary", "must be a number");
        }
        return new EmployeeInput(r.get("firstName"), r.get("lastName"), r.get("email"), r.get("country"),
                r.get("department"), r.get("level"), r.get("jobTitle"), r.get("gender"), hireDate, salary,
                r.get("currency"));
    }

    private void persist(EmployeeInput in) {
        Employee e = new Employee();
        e.setFirstName(in.firstName());
        e.setLastName(in.lastName());
        e.setEmail(in.email());
        e.setCountry(in.country());
        e.setDepartment(in.department());
        e.setLevel(in.level());
        e.setJobTitle(in.jobTitle());
        e.setGender(in.gender());
        e.setHireDate(in.hireDate());
        e.setSalary(in.salary());
        e.setCurrency(in.currency());
        e.setSalaryUsd(fx.toUsd(in.salary(), in.currency()));
        employees.save(e);
        history.append(e.getId(), null, e.getSalary(), e.getCurrency(), REASON, SalaryChangeInput.DEFAULT_CHANGED_BY);
    }

    private static CSVParser open(InputStream in) throws IOException {
        PushbackReader reader = new PushbackReader(new InputStreamReader(in, StandardCharsets.UTF_8), 1);
        int first = reader.read();
        if (first != -1 && first != '﻿') {
            reader.unread(first); // strip a UTF-8 BOM if present
        }
        Reader r = reader;
        return CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true)
                .setIgnoreHeaderCase(true).setTrim(true).get().parse(r);
    }

    private static void checkHeader(Set<String> actual) {
        Set<String> lower = actual.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<String> missing = CsvColumns.HEADER.stream().filter(c -> !lower.contains(c.toLowerCase())).toList();
        if (!missing.isEmpty()) {
            throw new ValidationException("header", "missing columns: " + String.join(", ", missing)
                    + ". Expected: " + String.join(",", CsvColumns.HEADER));
        }
    }
}
