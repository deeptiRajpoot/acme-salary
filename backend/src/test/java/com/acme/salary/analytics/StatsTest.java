package com.acme.salary.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class StatsTest {

    @Test
    void meanAndPopulationStdDev() {
        double[] v = {2, 4, 4, 4, 5, 5, 7, 9};
        assertThat(Stats.mean(v)).isEqualTo(5.0);
        assertThat(Stats.stdDev(v)).isCloseTo(2.0, within(1e-12));
    }

    @Test
    void singleValueHasZeroDeviation() {
        assertThat(Stats.stdDev(new double[] {10})).isZero();
    }

    @Test
    void emptyIsRejected() {
        assertThatThrownBy(() -> Stats.mean(new double[0])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Stats.stdDev(new double[0])).isInstanceOf(IllegalArgumentException.class);
    }
}
