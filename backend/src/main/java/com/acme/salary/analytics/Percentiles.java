package com.acme.salary.analytics;

import java.util.Arrays;

/** Pure percentile math (linear interpolation between closest ranks, a.k.a. Excel PERCENTILE.INC / R-7). */
public final class Percentiles {

    private Percentiles() {
    }

    /** @param p percentile in [0, 100]; values need not be sorted and are not modified */
    public static double percentile(double[] values, double p) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        return percentileSorted(sorted, p);
    }

    public static double percentileSorted(double[] sorted, double p) {
        if (sorted.length == 0) {
            throw new IllegalArgumentException("values must not be empty");
        }
        if (p < 0 || p > 100 || Double.isNaN(p)) {
            throw new IllegalArgumentException("percentile must be within [0, 100]: " + p);
        }
        double rank = p / 100.0 * (sorted.length - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        return sorted[lo] + (sorted[hi] - sorted[lo]) * (rank - lo);
    }

    public static double median(double[] values) {
        return percentile(values, 50);
    }
}
