package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.employee.EmployeeService;
import com.acme.salary.support.IntegrationTest;
import com.acme.salary.support.TestData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class AnalyticsApiTest {

    @Autowired
    EmployeeService employees;
    @Autowired
    AnalyticsService analytics;
    @Autowired
    MockMvc mvc;

    @BeforeEach
    void data() {
        for (int i = 0; i < 10; i++) { // US/Engineering/L3 peer group with one clear outlier
            String sal = i == 9 ? "400000" : String.valueOf(100_000 + i * 1000);
            employees.create(TestData.input("m" + i + "@x.com", "M" + i, "US", "Engineering", "L3",
                    i % 2 == 0 ? "M" : "F", sal, "USD"));
        }
        for (int i = 0; i < 4; i++) {
            employees.create(TestData.input("g" + i + "@x.com", "G" + i, "GB", "Sales", "L1", "M", "40000", "GBP"));
        }
    }

    @Test
    void summaryMatchesHandComputedValues() {
        AnalyticsDtos.Summary s = analytics.summary();
        assertThat(s.headcount()).isEqualTo(14);
        assertThat(s.countries()).isEqualTo(2);
        assertThat(s.departments()).isEqualTo(2);
        assertThat(s.totalPayrollUsd()).isEqualTo(100_000 * 9 + 36_000 + 400_000 + 4 * 50_800.0);
        assertThat(s.p25Usd()).isLessThanOrEqualTo(s.medianUsd());
        assertThat(s.medianUsd()).isLessThanOrEqualTo(s.p75Usd());
        assertThat(s.p75Usd()).isLessThanOrEqualTo(s.p90Usd());
    }

    @Test
    void groupStatsAreSortedAndCountCorrectly() {
        List<AnalyticsDtos.GroupStats> c = analytics.byCountry();
        assertThat(c).extracting(AnalyticsDtos.GroupStats::group).containsExactly("GB", "US");
        assertThat(c.get(0).headcount()).isEqualTo(4);
        assertThat(c.get(0).medianUsd()).isEqualTo(50_800.0);
        assertThat(analytics.byLevel()).extracting(AnalyticsDtos.GroupStats::group).containsExactly("L1", "L3");
    }

    @Test
    void outlierEndpointFindsPlantedOutlier() throws Exception {
        mvc.perform(get("/api/analytics/outliers?limit=5")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].salaryUsd").value(400000.0))
                .andExpect(jsonPath("$[0].country").value("US"))
                .andExpect(jsonPath("$[0].zScore").isNumber())
                .andExpect(jsonPath("$[0].peerMedianUsd").isNumber());
        mvc.perform(get("/api/analytics/outliers?limit=0")).andExpect(status().isBadRequest());
    }

    @Test
    void distributionAndOtherEndpointsRespond() throws Exception {
        mvc.perform(get("/api/analytics/distribution?buckets=4")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/analytics/distribution?buckets=0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/analytics/summary")).andExpect(status().isOk()).andExpect(jsonPath("$.headcount").value(14));
        mvc.perform(get("/api/analytics/by-department")).andExpect(status().isOk());
        mvc.perform(get("/api/analytics/pay-gap")).andExpect(status().isOk())
                .andExpect(jsonPath("$.overallGapPercent").isNumber()).andExpect(jsonPath("$.cohorts").isArray());
    }

    @Test
    void payGapCohortNeedsThreePerGender() {
        AnalyticsDtos.PayGap gap = analytics.payGap();
        assertThat(gap.cohorts()).singleElement().satisfies(c -> {
            assertThat(c.department()).isEqualTo("Engineering");
            assertThat(c.maleCount()).isEqualTo(5);
            assertThat(c.femaleCount()).isEqualTo(5);
        });
    }
}
