package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class HistogramTest {

    @Test
    void maxValueLandsInLastBucketAndCountsAddUp() {
        List<Histogram.Bucket> b = Histogram.build(new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5);
        assertThat(b).hasSize(5);
        assertThat(b.stream().mapToInt(Histogram.Bucket::count).sum()).isEqualTo(11);
        assertThat(b.get(4).count()).isEqualTo(3); // 8,9,10
        assertThat(b.get(4).toUsd()).isEqualTo(10);
    }

    @Test
    void identicalValuesCollapseToOneBucket() {
        assertThat(Histogram.build(new double[] {5, 5}, 10)).containsExactly(new Histogram.Bucket(5, 5, 2));
    }

    @Test
    void emptyAndInvalid() {
        assertThat(Histogram.build(new double[0], 4)).isEmpty();
        assertThatThrownBy(() -> Histogram.build(new double[] {1}, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
