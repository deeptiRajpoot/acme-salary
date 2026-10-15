package com.acme.salary.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public record SalaryHistoryEntry(Long id, BigDecimal oldSalary, BigDecimal newSalary, String currency,
                                 String reason, String changedBy, Instant changedAt) {

    static SalaryHistoryEntry from(SalaryHistory h) {
        return new SalaryHistoryEntry(h.getId(), scale(h.getOldSalary()), scale(h.getNewSalary()), h.getCurrency(),
                h.getReason(), h.getChangedBy(), h.getChangedAt());
    }

    private static BigDecimal scale(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }
}
