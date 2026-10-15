package com.acme.salary.importexport;

import com.acme.salary.common.ValidationException;
import com.acme.salary.employee.EmployeeFilter;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api")
public class ImportExportController {

    private final ImportService importService;
    private final ExportService exportService;

    public ImportExportController(ImportService importService, ExportService exportService) {
        this.importService = importService;
        this.exportService = exportService;
    }

    @PostMapping("/import/employees")
    public ImportResult importEmployees(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ValidationException("file", "is empty");
        }
        return importService.importCsv(file.getInputStream());
    }

    @GetMapping("/export/employees")
    public ResponseEntity<StreamingResponseBody> export(@RequestParam(required = false) String q,
                                                        @RequestParam(required = false) String country,
                                                        @RequestParam(required = false) String department,
                                                        @RequestParam(required = false) String level) {
        EmployeeFilter filter = new EmployeeFilter(q, country, department, level);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .body(out -> exportService.write(filter, out));
    }
}
