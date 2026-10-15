package com.acme.salary.salary;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Writes and reads the append-only salary audit trail. */
@Service
public class SalaryHistoryService {

    private final SalaryHistoryRepository repository;
    private final Clock clock;

    public SalaryHistoryService(SalaryHistoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void append(Long employeeId, BigDecimal oldSalary, BigDecimal newSalary, String currency,
                       String reason, String changedBy) {
        repository.save(new SalaryHistory(employeeId, oldSalary, newSalary, currency, reason, changedBy,
                Instant.now(clock)));
    }

    @Transactional(readOnly = true)
    public List<SalaryHistoryEntry> list(Long employeeId) {
        return repository.findByEmployeeIdOrderByIdDesc(employeeId).stream().map(SalaryHistoryEntry::from).toList();
    }

    @Transactional
    public void deleteAllFor(Long employeeId) {
        repository.deleteByEmployeeId(employeeId);
    }
}
