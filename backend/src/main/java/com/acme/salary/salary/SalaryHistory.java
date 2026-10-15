package com.acme.salary.salary;

import com.acme.salary.common.IsoInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Append-only audit row; there are intentionally no setters and no update path. */
@Entity
@Table(name = "salary_history")
public class SalaryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "old_salary")
    private BigDecimal oldSalary;

    @Column(name = "new_salary", nullable = false)
    private BigDecimal newSalary;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String reason;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Convert(converter = IsoInstantConverter.class)
    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected SalaryHistory() {
    }

    public SalaryHistory(Long employeeId, BigDecimal oldSalary, BigDecimal newSalary, String currency,
                         String reason, String changedBy, Instant changedAt) {
        this.employeeId = employeeId;
        this.oldSalary = oldSalary;
        this.newSalary = newSalary;
        this.currency = currency;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public BigDecimal getOldSalary() { return oldSalary; }
    public BigDecimal getNewSalary() { return newSalary; }
    public String getCurrency() { return currency; }
    public String getReason() { return reason; }
    public String getChangedBy() { return changedBy; }
    public Instant getChangedAt() { return changedAt; }
}
