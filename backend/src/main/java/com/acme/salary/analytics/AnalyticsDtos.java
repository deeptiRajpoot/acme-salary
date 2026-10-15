package com.acme.salary.analytics;

import java.util.List;

/** Response shapes of /api/analytics/* (see docs/openapi.yaml). */
public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    public record Summary(int headcount, double totalPayrollUsd, double meanUsd, double medianUsd, double p25Usd,
                          double p75Usd, double p90Usd, int countries, int departments) {
    }

    public record GroupStats(String group, int headcount, double minUsd, double p25Usd, double medianUsd,
                             double p75Usd, double p90Usd, double maxUsd, double meanUsd) {
    }

    public record Bucket(double fromUsd, double toUsd, int count) {
    }

    public record PayGap(double overallGapPercent, List<PayGapCohort> cohorts) {
    }

    public record PayGapCohort(String department, String level, double medianFemaleUsd, double medianMaleUsd,
                               double gapPercent, int femaleCount, int maleCount) {
    }

    public record Outlier(long employeeId, String name, String country, String department, String level,
                          double salaryUsd, double peerMedianUsd, double zScore) {
    }
}
