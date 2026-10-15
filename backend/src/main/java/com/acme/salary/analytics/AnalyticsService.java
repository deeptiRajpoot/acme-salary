package com.acme.salary.analytics;

import com.acme.salary.common.ValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    static final int MAX_BUCKETS = 200;
    static final int MAX_OUTLIER_LIMIT = 1000;

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public AnalyticsDtos.Summary summary() {
        List<PayRow> rows = repository.findAllPayRows();
        if (rows.isEmpty()) {
            return new AnalyticsDtos.Summary(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        double[] v = salaries(rows);
        double total = 0;
        for (double s : v) {
            total += s;
        }
        return new AnalyticsDtos.Summary(rows.size(), round(total), round(Stats.mean(v)),
                round(Percentiles.percentile(v, 50)), round(Percentiles.percentile(v, 25)),
                round(Percentiles.percentile(v, 75)), round(Percentiles.percentile(v, 90)),
                (int) rows.stream().map(PayRow::country).distinct().count(),
                (int) rows.stream().map(PayRow::department).distinct().count());
    }

    public List<AnalyticsDtos.GroupStats> byCountry() {
        return grouped(PayRow::country);
    }

    public List<AnalyticsDtos.GroupStats> byDepartment() {
        return grouped(PayRow::department);
    }

    public List<AnalyticsDtos.GroupStats> byLevel() {
        return grouped(PayRow::level);
    }

    public List<AnalyticsDtos.Bucket> distribution(int buckets) {
        if (buckets < 1 || buckets > MAX_BUCKETS) {
            throw new ValidationException("buckets", "must be between 1 and " + MAX_BUCKETS);
        }
        return Histogram.build(salaries(repository.findAllPayRows()), buckets).stream()
                .map(b -> new AnalyticsDtos.Bucket(round(b.fromUsd()), round(b.toUsd()), b.count())).toList();
    }

    public AnalyticsDtos.PayGap payGap() {
        PayGapCalculator.Result r = PayGapCalculator.compute(repository.findAllPayRows().stream()
                .map(p -> new PayGapCalculator.Person(p.department(), p.level(), p.gender(), p.salaryUsd())).toList());
        return new AnalyticsDtos.PayGap(round(r.overallGapPercent()), r.cohorts().stream()
                .map(c -> new AnalyticsDtos.PayGapCohort(c.department(), c.level(), round(c.medianFemaleUsd()),
                        round(c.medianMaleUsd()), round(c.gapPercent()), c.femaleCount(), c.maleCount()))
                .toList());
    }

    public List<AnalyticsDtos.Outlier> outliers(int limit) {
        if (limit < 1 || limit > MAX_OUTLIER_LIMIT) {
            throw new ValidationException("limit", "must be between 1 and " + MAX_OUTLIER_LIMIT);
        }
        return OutlierDetector.detect(repository.findAllPayRows().stream()
                        .map(p -> new OutlierDetector.Person(p.id(), p.name(), p.country(), p.department(),
                                p.level(), p.salaryUsd())).toList())
                .stream().limit(limit)
                .map(o -> new AnalyticsDtos.Outlier(o.employeeId(), o.name(), o.country(), o.department(),
                        o.level(), round(o.salaryUsd()), round(o.peerMedianUsd()), round(o.zScore())))
                .toList();
    }

    private List<AnalyticsDtos.GroupStats> grouped(Function<PayRow, String> key) {
        Map<String, List<PayRow>> groups = repository.findAllPayRows().stream()
                .collect(Collectors.groupingBy(key, TreeMap::new, Collectors.toList()));
        List<AnalyticsDtos.GroupStats> out = new ArrayList<>();
        groups.forEach((name, rows) -> {
            double[] v = salaries(rows);
            java.util.Arrays.sort(v);
            out.add(new AnalyticsDtos.GroupStats(name, v.length, round(v[0]),
                    round(Percentiles.percentileSorted(v, 25)), round(Percentiles.percentileSorted(v, 50)),
                    round(Percentiles.percentileSorted(v, 75)), round(Percentiles.percentileSorted(v, 90)),
                    round(v[v.length - 1]), round(Stats.mean(v))));
        });
        out.sort(Comparator.comparing(AnalyticsDtos.GroupStats::group));
        return out;
    }

    private static double[] salaries(List<PayRow> rows) {
        return rows.stream().mapToDouble(PayRow::salaryUsd).toArray();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
