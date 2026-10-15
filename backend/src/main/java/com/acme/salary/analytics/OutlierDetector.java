package com.acme.salary.analytics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure z-score outlier detection inside peer groups (country + department + level).
 * Groups smaller than {@link #MIN_GROUP_SIZE} are skipped (too few peers to be meaningful);
 * flagged when |z| is strictly greater than {@link #Z_THRESHOLD}.
 */
public final class OutlierDetector {

    public static final int MIN_GROUP_SIZE = 5;
    public static final double Z_THRESHOLD = 2.0;

    private OutlierDetector() {
    }

    public record Person(long id, String name, String country, String department, String level, double salaryUsd) {
    }

    public record Outlier(long employeeId, String name, String country, String department, String level,
                          double salaryUsd, double peerMedianUsd, double zScore) {
    }

    /** @return outliers ordered by |z| descending (ties by id) */
    public static List<Outlier> detect(List<Person> people) {
        Map<String, List<Person>> groups = new LinkedHashMap<>();
        for (Person p : people) {
            groups.computeIfAbsent(p.country() + "|" + p.department() + "|" + p.level(), k -> new ArrayList<>()).add(p);
        }
        List<Outlier> result = new ArrayList<>();
        for (List<Person> group : groups.values()) {
            if (group.size() >= MIN_GROUP_SIZE) {
                flag(group, result);
            }
        }
        result.sort(Comparator.comparingDouble((Outlier o) -> Math.abs(o.zScore())).reversed()
                .thenComparingLong(Outlier::employeeId));
        return result;
    }

    private static void flag(List<Person> group, List<Outlier> out) {
        double[] salaries = group.stream().mapToDouble(Person::salaryUsd).toArray();
        double sd = Stats.stdDev(salaries);
        if (sd == 0) {
            return;
        }
        double mean = Stats.mean(salaries);
        double median = Percentiles.median(salaries);
        for (Person p : group) {
            double z = (p.salaryUsd() - mean) / sd;
            if (Math.abs(z) > Z_THRESHOLD) {
                out.add(new Outlier(p.id(), p.name(), p.country(), p.department(), p.level(), p.salaryUsd(),
                        median, z));
            }
        }
    }
}
