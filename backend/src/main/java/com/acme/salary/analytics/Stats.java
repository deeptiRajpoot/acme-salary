package com.acme.salary.analytics;

/** Pure descriptive statistics. */
public final class Stats {

    private Stats() {
    }

    public static double mean(double[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("values must not be empty");
        }
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /** Population standard deviation (the whole peer group is the population). 0 for a single value. */
    public static double stdDev(double[] values) {
        double mean = mean(values);
        double sq = 0;
        for (double v : values) {
            sq += (v - mean) * (v - mean);
        }
        return Math.sqrt(sq / values.length);
    }

    public static double min(double[] values) {
        return java.util.Arrays.stream(values).min().orElseThrow(() -> new IllegalArgumentException("empty"));
    }

    public static double max(double[] values) {
        return java.util.Arrays.stream(values).max().orElseThrow(() -> new IllegalArgumentException("empty"));
    }
}
