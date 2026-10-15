package com.acme.salary.importexport;

import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeFilter;
import com.acme.salary.employee.EmployeeService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

/** Streams matching employees as CSV in id-ordered chunks, so memory stays flat at any size. */
@Service
public class ExportService {

    private static final int CHUNK = 1000;

    private final EmployeeService employees;

    public ExportService(EmployeeService employees) {
        this.employees = employees;
    }

    public void write(EmployeeFilter filter, OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        try (CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setRecordSeparator("\r\n").get())) {
            csv.printRecord(CsvColumns.HEADER);
            long lastId = 0;
            List<Employee> chunk;
            while (!(chunk = employees.chunkAfter(filter, lastId, CHUNK)).isEmpty()) {
                for (Employee e : chunk) {
                    csv.printRecord(safe(e.getFirstName()), safe(e.getLastName()), e.getEmail(), e.getCountry(),
                            e.getDepartment(), e.getLevel(), safe(e.getJobTitle()), e.getGender(), e.getHireDate(),
                            e.getSalary().stripTrailingZeros().toPlainString(), e.getCurrency());
                    lastId = e.getId();
                }
                csv.flush();
            }
        }
    }

    /** Neutralises spreadsheet formula injection (cells starting with = + - @). */
    static String safe(String value) {
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }
}
