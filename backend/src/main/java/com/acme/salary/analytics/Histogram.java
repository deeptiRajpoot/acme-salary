package com.acme.salary.analytics;

import java.util.ArrayList;
import java.util.List;

/** Pure equal-width histogram. The last bucket includes the maximum value. */
public final class Histogram {

    private Histogram() {
    }

    public record Bucket(double fromUsd, double toUsd, int count) {
    }

    public static List<Bucket> build(double[] values, int buckets) {
        if (buckets < 1) {
            throw new IllegalArgumentException("buckets must be >= 1");
        }
        if (values.length == 0) {
            return List.of();
        }
        double min = Stats.min(values);
        double max = Stats.max(values);
        if (min == max) {
            return List.of(new Bucket(min, max, values.length));
        }
        double width = (max - min) / buckets;
        int[] counts = new int[buckets];
        for (double v : values) {
            counts[Math.min(buckets - 1, (int) ((v - min) / width))]++;
        }
        List<Bucket> result = new ArrayList<>(buckets);
        for (int i = 0; i < buckets; i++) {
            result.add(new Bucket(min + i * width, i == buckets - 1 ? max : min + (i + 1) * width, counts[i]));
        }
        return result;
    }
}
