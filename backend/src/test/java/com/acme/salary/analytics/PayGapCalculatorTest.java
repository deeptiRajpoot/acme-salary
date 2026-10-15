package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayGapCalculatorTest {

    private static void add(List<PayGapCalculator.Person> list, String dept, String level, String g, int n, double pay) {
        for (int i = 0; i < n; i++) {
            list.add(new PayGapCalculator.Person(dept, level, g, pay + i));
        }
    }

    @Test
    void cohortGapUsesMedians() {
        List<PayGapCalculator.Person> p = new ArrayList<>();
        add(p, "Eng", "L3", "M", 3, 100); // median 101
        add(p, "Eng", "L3", "F", 3, 90);  // median 91
        PayGapCalculator.Result r = PayGapCalculator.compute(p);
        assertThat(r.cohorts()).singleElement().satisfies(c -> {
            assertThat(c.medianMaleUsd()).isEqualTo(101);
            assertThat(c.medianFemaleUsd()).isEqualTo(91);
            assertThat(c.gapPercent()).isCloseTo(10.0 / 101 * 100, within(1e-9));
            assertThat(c.femaleCount()).isEqualTo(3);
        });
        assertThat(r.overallGapPercent()).isCloseTo(10.0 / 101 * 100, within(1e-9));
    }

    @Test
    void womenPaidMoreGivesNegativeGap() {
        List<PayGapCalculator.Person> p = new ArrayList<>();
        add(p, "Eng", "L3", "M", 3, 100);
        add(p, "Eng", "L3", "F", 3, 120);
        assertThat(PayGapCalculator.compute(p).overallGapPercent()).isNegative();
    }

    @Test
    void smallCohortsAndGenderXAreIgnored() {
        List<PayGapCalculator.Person> p = new ArrayList<>();
        add(p, "Eng", "L3", "M", 5, 100);
        add(p, "Eng", "L3", "F", 2, 50);   // too few women
        add(p, "Eng", "L3", "X", 9, 10);   // not compared
        PayGapCalculator.Result r = PayGapCalculator.compute(p);
        assertThat(r.cohorts()).isEmpty();
        assertThat(r.overallGapPercent()).isZero();
    }

    @Test
    void overallGapIsHeadcountWeighted() {
        List<PayGapCalculator.Person> p = new ArrayList<>();
        add(p, "Eng", "L1", "M", 3, 100);
        add(p, "Eng", "L1", "F", 3, 100);   // gap 0, weight 6
        add(p, "Ops", "L1", "M", 9, 100);
        add(p, "Ops", "L1", "F", 9, 80);    // median 84 vs 104 -> gap ~19.2, weight 18
        PayGapCalculator.Result r = PayGapCalculator.compute(p);
        assertThat(r.cohorts()).hasSize(2);
        double ops = r.cohorts().get(1).gapPercent();
        assertThat(r.overallGapPercent()).isCloseTo(ops * 18 / 24, within(1e-9));
    }

    @Test
    void emptyInput() {
        assertThat(PayGapCalculator.compute(List.of()).cohorts()).isEmpty();
    }
}
