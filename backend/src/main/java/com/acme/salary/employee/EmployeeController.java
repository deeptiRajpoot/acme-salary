package com.acme.salary.employee;

import com.acme.salary.salary.SalaryChangeInput;
import com.acme.salary.salary.SalaryHistoryEntry;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public EmployeePageResponse search(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "25") int size,
                                       @RequestParam(defaultValue = "lastName,asc") String sort,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) String country,
                                       @RequestParam(required = false) String department,
                                       @RequestParam(required = false) String level) {
        return service.search(new EmployeeFilter(q, country, department, level), page, size, sort);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody EmployeeInput input) {
        EmployeeResponse created = service.create(input);
        return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable long id, @RequestBody EmployeeInput input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/salary")
    public EmployeeResponse changeSalary(@PathVariable long id, @Valid @RequestBody SalaryChangeInput input) {
        return service.changeSalary(id, input);
    }

    @GetMapping("/{id}/salary-history")
    public List<SalaryHistoryEntry> salaryHistory(@PathVariable long id) {
        return service.salaryHistory(id);
    }
}
