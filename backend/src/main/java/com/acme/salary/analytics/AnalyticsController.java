package com.acme.salary.analytics;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public AnalyticsDtos.Summary summary() {
        return service.summary();
    }

    @GetMapping("/by-country")
    public List<AnalyticsDtos.GroupStats> byCountry() {
        return service.byCountry();
    }

    @GetMapping("/by-department")
    public List<AnalyticsDtos.GroupStats> byDepartment() {
        return service.byDepartment();
    }

    @GetMapping("/by-level")
    public List<AnalyticsDtos.GroupStats> byLevel() {
        return service.byLevel();
    }

    @GetMapping("/distribution")
    public List<AnalyticsDtos.Bucket> distribution(@RequestParam(defaultValue = "20") int buckets) {
        return service.distribution(buckets);
    }

    @GetMapping("/pay-gap")
    public AnalyticsDtos.PayGap payGap() {
        return service.payGap();
    }

    @GetMapping("/outliers")
    public List<AnalyticsDtos.Outlier> outliers(@RequestParam(defaultValue = "50") int limit) {
        return service.outliers(limit);
    }
}
