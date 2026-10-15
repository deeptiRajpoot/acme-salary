package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PercentilesTest {

    @Test
    void emptyInputIsRejected() {
        assertThatThrownBy(() -> Percentiles.percentile(new double[0], 50)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void singleValueIsEveryPercentile() {
        assertThat(Percentiles.percentile(new double[] {42}, 0)).isEqualTo(42);
        assertThat(Percentiles.percentile(new double[] {42}, 90)).isEqualTo(42);
    }

    @Test
    void interpolatesLinearlyBetweenRanks() {
        double[] v = {10, 20, 30, 40};
        assertThat(Percentiles.percentile(v, 50)).isEqualTo(25.0);   // rank 1.5
        assertThat(Percentiles.percentile(v, 25)).isEqualTo(17.5);   // rank 0.75
        assertThat(Percentiles.percentile(v, 90)).isCloseTo(37.0, org.assertj.core.data.Offset.offset(1e-9)); // rank 2.7
    }

    @Test
    void endpointsAreMinAndMax() {
        double[] v = {5, 1, 9, 3};
        assertThat(Percentiles.percentile(v, 0)).isEqualTo(1);
        assertThat(Percentiles.percentile(v, 100)).isEqualTo(9);
    }

    @Test
    void unsortedInputIsNotMutatedAndTiesWork() {
        double[] v = {7, 7, 7, 1};
        assertThat(Percentiles.median(v)).isEqualTo(7);
        assertThat(v).containsExactly(7, 7, 7, 1);
    }

    @Test
    void outOfRangePercentileIsRejected() {
        assertThatThrownBy(() -> Percentiles.percentile(new double[] {1}, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Percentiles.percentile(new double[] {1}, -1)).isInstanceOf(IllegalArgumentException.class);
    }
}
