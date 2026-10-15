package com.acme.salary.analytics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pure cohort pay-gap math. Cohorts are department + level; a cohort needs at least
 * {@link #MIN_PER_GENDER} women and men (gender X is not part of the F/M comparison).
 * gap% = (median M - median F) / median M * 100; the overall gap is the headcount-weighted mean of cohort gaps.
 */
public final class PayGapCalculator {

    public static final int MIN_PER_GENDER = 3;

    private PayGapCalculator() {
    }

    public record Person(String department, String level, String gender, double salaryUsd) {
    }

    public record Cohort(String department, String level, double medianFemaleUsd, double medianMaleUsd,
                         double gapPercent, int femaleCount, int maleCount) {
    }

    public record Result(double overallGapPercent, List<Cohort> cohorts) {
    }

    public static Result compute(List<Person> people) {
        Map<String, Map<String, List<Double>>> byCohort = new TreeMap<>();
        for (Person p : people) {
            if (p.gender().equals("F") || p.gender().equals("M")) {
                byCohort.computeIfAbsent(p.department() + "\u0000" + p.level(), k -> new TreeMap<>())
                        .computeIfAbsent(p.gender(), k -> new ArrayList<>()).add(p.salaryUsd());
            }
        }
        List<Cohort> cohorts = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<Double>>> e : byCohort.entrySet()) {
            List<Double> f = e.getValue().getOrDefault("F", List.of());
            List<Double> m = e.getValue().getOrDefault("M", List.of());
            if (f.size() < MIN_PER_GENDER || m.size() < MIN_PER_GENDER) {
                continue;
            }
            String[] key = e.getKey().split("\u0000");
            double medF = Percentiles.median(toArray(f));
            double medM = Percentiles.median(toArray(m));
            cohorts.add(new Cohort(key[0], key[1], medF, medM, (medM - medF) / medM * 100.0, f.size(), m.size()));
        }
        cohorts.sort(Comparator.comparing(Cohort::department).thenComparing(Cohort::level));
        double weighted = 0;
        long weight = 0;
        for (Cohort c : cohorts) {
            int w = c.femaleCount() + c.maleCount();
            weighted += c.gapPercent() * w;
            weight += w;
        }
        return new Result(weight == 0 ? 0.0 : weighted / weight, cohorts);
    }

    private static double[] toArray(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
