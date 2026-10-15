package com.acme.salary.employee;

import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.ValidationException;
import com.acme.salary.salary.FxConverter;
import com.acme.salary.salary.SalaryChangeInput;
import com.acme.salary.salary.SalaryHistoryEntry;
import com.acme.salary.salary.SalaryHistoryService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    public static final int MAX_PAGE_SIZE = 200;
    private static final String INITIAL_REASON = "Initial hire";
    /** API field name -> entity property; anything else is rejected (no arbitrary column sorting). */
    private static final List<String> SORT_FIELDS = List.of("id", "firstName", "lastName", "email", "country",
            "department", "level", "jobTitle", "gender", "hireDate", "salary", "currency", "salaryUsd");

    private final EmployeeRepository repository;
    private final EmployeeValidator validator;
    private final SalaryHistoryService history;
    private final FxConverter fx;

    public EmployeeService(EmployeeRepository repository, EmployeeValidator validator,
                           SalaryHistoryService history, FxConverter fx) {
        this.repository = repository;
        this.validator = validator;
        this.history = history;
        this.fx = fx;
    }

    @Transactional(readOnly = true)
    public EmployeePageResponse search(EmployeeFilter filter, int page, int size, String sort) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (page < 0) {
            errors.put("page", "must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            errors.put("size", "must be between 1 and " + MAX_PAGE_SIZE);
        }
        Sort order = parseSort(sort, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        Page<Employee> result = repository.findAll(EmployeeSpecifications.matching(filter),
                PageRequest.of(page, size, order));
        return new EmployeePageResponse(result.getContent().stream().map(EmployeeResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    /** Keyset-paged chunk (id ascending) for streaming exports. */
    @Transactional(readOnly = true)
    public List<Employee> chunkAfter(EmployeeFilter filter, long afterId, int limit) {
        return repository.findAll(EmployeeSpecifications.matching(filter).and(EmployeeSpecifications.idAfter(afterId)),
                PageRequest.of(0, limit, Sort.by("id"))).getContent();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(long id) {
        return EmployeeResponse.from(find(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeInput raw) {
        EmployeeInput in = raw.normalized();
        validator.validateOrThrow(in, validator.referenceData(), repository::existsByEmail);
        Employee e = new Employee();
        applyProfile(e, in);
        e.setSalary(in.salary());
        e.setCurrency(in.currency());
        e.setSalaryUsd(fx.toUsd(in.salary(), in.currency()));
        save(e);
        history.append(e.getId(), null, e.getSalary(), e.getCurrency(), INITIAL_REASON,
                SalaryChangeInput.DEFAULT_CHANGED_BY);
        return EmployeeResponse.from(e);
    }

    /** Profile update only: salary/currency must be unchanged; use {@link #changeSalary} for pay. */
    @Transactional
    public EmployeeResponse update(long id, EmployeeInput raw) {
        Employee e = find(id);
        EmployeeInput in = raw.normalized();
        Map<String, String> errors = new LinkedHashMap<>(
                validator.validate(in, validator.referenceData(), email -> repository.existsByEmailAndIdNot(email, id)));
        if (in.salary() != null && in.salary().compareTo(e.getSalary()) != 0) {
            errors.put("salary", "cannot be changed here; use POST /employees/{id}/salary");
        }
        if (in.currency() != null && !in.currency().equals(e.getCurrency())) {
            errors.put("currency", "cannot be changed here; use POST /employees/{id}/salary");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        applyProfile(e, in);
        save(e);
        return EmployeeResponse.from(e);
    }

    @Transactional
    public EmployeeResponse changeSalary(long id, SalaryChangeInput in) {
        Employee e = find(id);
        BigDecimal old = e.getSalary();
        e.setSalary(in.newSalary());
        e.setSalaryUsd(fx.toUsd(in.newSalary(), e.getCurrency()));
        repository.save(e);
        history.append(id, old, in.newSalary(), e.getCurrency(), in.reason().trim(), in.changedByOrDefault());
        return EmployeeResponse.from(e);
    }

    @Transactional(readOnly = true)
    public List<SalaryHistoryEntry> salaryHistory(long id) {
        find(id);
        return history.list(id);
    }

    @Transactional
    public void delete(long id) {
        Employee e = find(id);
        history.deleteAllFor(id);
        repository.delete(e);
    }

    private Employee find(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Employee " + id + " not found"));
    }

    private void save(Employee e) {
        try {
            repository.saveAndFlush(e);
        } catch (DataIntegrityViolationException ex) {
            // lost a race with a concurrent insert of the same email
            throw new ValidationException("email", "is already in use");
        }
    }

    private static void applyProfile(Employee e, EmployeeInput in) {
        e.setFirstName(in.firstName());
        e.setLastName(in.lastName());
        e.setEmail(in.email());
        e.setCountry(in.country());
        e.setDepartment(in.department());
        e.setLevel(in.level());
        e.setJobTitle(in.jobTitle());
        e.setGender(in.gender());
        e.setHireDate(in.hireDate());
    }

    private static Sort parseSort(String sort, Map<String, String> errors) {
        String[] parts = (sort == null || sort.isBlank() ? "lastName,asc" : sort).split(",");
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
        if (!SORT_FIELDS.contains(field)) {
            errors.put("sort", "unsupported field '" + field + "'; allowed: " + String.join(", ", SORT_FIELDS));
            return Sort.unsorted();
        }
        if (!dir.equals("asc") && !dir.equals("desc")) {
            errors.put("sort", "direction must be asc or desc");
            return Sort.unsorted();
        }
        Sort primary = Sort.by(dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, field);
        return field.equals("id") ? primary : primary.and(Sort.by("id")); // stable paging
    }
}
