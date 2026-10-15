package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OutlierDetectorTest {

    private static List<OutlierDetector.Person> group(String country, int n, double base, double... extras) {
        List<OutlierDetector.Person> people = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            people.add(new OutlierDetector.Person(people.size() + 1, "P" + i, country, "Eng", "L3", base + (i % 3)));
        }
        for (double x : extras) {
            people.add(new OutlierDetector.Person(1000 + people.size(), "X", country, "Eng", "L3", x));
        }
        return people;
    }

    @Test
    void flagsClearOutlierWithPeerMedian() {
        List<OutlierDetector.Person> people = group("US", 12, 100_000, 300_000);
        List<OutlierDetector.Outlier> result = OutlierDetector.detect(people);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).salaryUsd()).isEqualTo(300_000);
        assertThat(result.get(0).zScore()).isGreaterThan(2);
        assertThat(result.get(0).peerMedianUsd()).isBetween(100_000.0, 100_003.0);
    }

    @Test
    void flagsLowOutlierWithNegativeZ() {
        List<OutlierDetector.Outlier> result = OutlierDetector.detect(group("US", 12, 100_000, 20_000));
        assertThat(result).singleElement().satisfies(o -> assertThat(o.zScore()).isLessThan(-2));
    }

    @Test
    void groupsBelowMinimumSizeAreSkipped() {
        // 4 peers + 1 extreme = 5 -> flagged group size is 5, only 4 here
        assertThat(OutlierDetector.detect(group("US", 3, 100_000, 900_000))).isEmpty();
    }

    @Test
    void peerGroupsAreSeparatedByCountry() {
        List<OutlierDetector.Person> people = new ArrayList<>(group("US", 10, 100_000));
        people.addAll(group("IN", 10, 30_000)); // cheap but normal within India
        assertThat(OutlierDetector.detect(people)).isEmpty();
    }

    @Test
    void identicalSalariesProduceNoOutliers() {
        List<OutlierDetector.Person> same = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            same.add(new OutlierDetector.Person(i, "P", "US", "Eng", "L1", 50_000));
        }
        assertThat(OutlierDetector.detect(same)).isEmpty();
    }

    @Test
    void emptyInput() {
        assertThat(OutlierDetector.detect(List.of())).isEmpty();
    }

    @Test
    void resultsAreOrderedByAbsoluteZ() {
        List<OutlierDetector.Person> people = new ArrayList<>(group("US", 20, 100_000, 250_000));
        people.addAll(group("GB", 20, 80_000, 10_000));
        List<OutlierDetector.Outlier> result = OutlierDetector.detect(people);
        assertThat(result).hasSize(2);
        assertThat(Math.abs(result.get(0).zScore())).isGreaterThanOrEqualTo(Math.abs(result.get(1).zScore()));
    }
}
