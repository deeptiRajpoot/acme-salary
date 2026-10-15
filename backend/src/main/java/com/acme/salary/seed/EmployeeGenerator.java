package com.acme.salary.seed;

import com.acme.salary.salary.FxConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Pure, deterministic employee generator: the same (seed, count, referenceDate, rates) always yields the
 * same list. It deliberately plants a story for the dashboard: a modest gender pay gap in three
 * departments (Engineering, Sales, Operations) and {@value #OUTLIER_COUNT} clear salary outliers.
 */
public final class EmployeeGenerator {

    public static final int OUTLIER_COUNT = 40;
    private static final int OUTLIER_MIN_GROUP = 15;
    private static final int HISTORY_YEARS = 15;

    private record Country(String code, double weight, double costFactor, String currency) {
    }

    /** Weights sum to 1; cost factor scales US-equivalent bands (local-currency = USD / FX rate). */
    private static final List<Country> COUNTRIES = List.of(
            new Country("US", 0.28, 1.00, "USD"), new Country("IN", 0.20, 0.32, "INR"),
            new Country("GB", 0.09, 0.82, "GBP"), new Country("DE", 0.09, 0.80, "EUR"),
            new Country("CA", 0.06, 0.85, "CAD"), new Country("AU", 0.05, 0.83, "AUD"),
            new Country("SG", 0.04, 0.88, "SGD"), new Country("BR", 0.07, 0.36, "BRL"),
            new Country("JP", 0.06, 0.75, "JPY"), new Country("FR", 0.06, 0.74, "EUR"));

    private record Dept(String name, double weight, double payFactor, double femaleShare, double femaleGapFactor,
                        String role) {
    }

    private static final List<Dept> DEPARTMENTS = List.of(
            new Dept("Engineering", 0.30, 1.08, 0.30, 0.93, "Software Engineer"),
            new Dept("Sales", 0.18, 1.00, 0.38, 0.95, "Account Executive"),
            new Dept("Operations", 0.15, 0.88, 0.45, 0.97, "Operations Analyst"),
            new Dept("Customer Support", 0.10, 0.80, 0.55, 1.0, "Support Specialist"),
            new Dept("Product", 0.08, 1.05, 0.42, 1.0, "Product Manager"),
            new Dept("Marketing", 0.08, 0.93, 0.60, 1.0, "Marketing Specialist"),
            new Dept("Finance", 0.07, 1.00, 0.50, 1.0, "Financial Analyst"),
            new Dept("Human Resources", 0.04, 0.90, 0.68, 1.0, "HR Partner"));

    private static final String[] LEVELS = {"L1", "L2", "L3", "L4", "L5", "L6", "L7"};
    private static final double[] LEVEL_WEIGHTS = {0.12, 0.22, 0.26, 0.20, 0.12, 0.06, 0.02};
    /** Mid-point of the US-equivalent annual base band per level, in USD. */
    private static final double[] LEVEL_MID_USD = {62_000, 82_000, 108_000, 140_000, 180_000, 235_000, 310_000};
    private static final double SALARY_NOISE = 0.07;
    private static final double NON_BINARY_SHARE = 0.03;

    private EmployeeGenerator() {
    }

    private static final class Draft {
        Country country;
        Dept dept;
        int level;
        String gender;
        String first;
        String last;
        LocalDate hireDate;
        double usd;
    }

    public static List<SeedEmployee> generate(long seed, int count, LocalDate referenceDate, FxConverter fx) {
        Random rnd = new Random(seed);
        List<Draft> drafts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            drafts.add(draft(rnd, referenceDate));
        }
        plantOutliers(drafts, rnd);
        return toEmployees(drafts, fx);
    }

    private static Draft draft(Random rnd, LocalDate referenceDate) {
        Draft d = new Draft();
        d.country = pick(rnd, COUNTRIES, Country::weight);
        d.dept = pick(rnd, DEPARTMENTS, Dept::weight);
        d.level = pickIndex(rnd, LEVEL_WEIGHTS);
        double g = rnd.nextDouble();
        d.gender = g < NON_BINARY_SHARE ? "X" : g < NON_BINARY_SHARE + (1 - NON_BINARY_SHARE) * d.dept.femaleShare() ? "F" : "M";
        SeedNames.Pool pool = SeedNames.forCountry(d.country.code());
        List<String> firsts = d.gender.equals("F") ? pool.female()
                : d.gender.equals("M") ? pool.male() : (rnd.nextBoolean() ? pool.female() : pool.male());
        d.first = firsts.get(rnd.nextInt(firsts.size()));
        d.last = pool.last().get(rnd.nextInt(pool.last().size()));
        // exponent > 1 skews hire dates towards recent years (growing company)
        int daysBack = (int) (HISTORY_YEARS * 365 * Math.pow(rnd.nextDouble(), 1.3));
        d.hireDate = referenceDate.minusDays(daysBack);
        d.usd = baseUsd(d, daysBack / 365.0, rnd);
        return d;
    }

    private static double baseUsd(Draft d, double tenureYears, Random rnd) {
        double tenure = 1 + 0.012 * Math.min(tenureYears, 10) - 0.06;
        double noise = Math.exp(Math.max(-1.6, Math.min(1.6, rnd.nextGaussian())) * SALARY_NOISE); // mild right skew
        double usd = LEVEL_MID_USD[d.level] * d.country.costFactor() * d.dept.payFactor() * tenure * noise;
        return d.gender.equals("F") ? usd * d.dept.femaleGapFactor() : usd;
    }

    private static void plantOutliers(List<Draft> drafts, Random rnd) {
        Map<String, Integer> sizes = new HashMap<>();
        drafts.forEach(d -> sizes.merge(groupKey(d), 1, Integer::sum));
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < drafts.size(); i++) {
            if (sizes.get(groupKey(drafts.get(i))) >= OUTLIER_MIN_GROUP) {
                candidates.add(i);
            }
        }
        Collections.shuffle(candidates, rnd);
        Set<String> usedGroups = new HashSet<>(); // at most one outlier per peer group keeps z-scores clean
        int planted = 0;
        for (int idx : candidates) {
            if (planted == OUTLIER_COUNT) {
                break;
            }
            Draft d = drafts.get(idx);
            if (!usedGroups.add(groupKey(d))) {
                continue;
            }
            boolean high = planted % 5 != 4; // 32 overpaid, 8 underpaid
            d.usd *= high ? 1.7 + rnd.nextDouble() * 0.4 : 0.45 + rnd.nextDouble() * 0.1;
            planted++;
        }
    }

    private static String groupKey(Draft d) {
        return d.country.code() + "|" + d.dept.name() + "|" + d.level;
    }

    private static List<SeedEmployee> toEmployees(List<Draft> drafts, FxConverter fx) {
        List<SeedEmployee> out = new ArrayList<>(drafts.size());
        Set<String> emails = new HashSet<>();
        for (Draft d : drafts) {
            BigDecimal rate = fx.rate(d.country.currency());
            BigDecimal local = BigDecimal.valueOf(d.usd).divide(rate, 0, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    .setScale(2);
            out.add(new SeedEmployee(d.first, d.last, uniqueEmail(d, emails), d.country.code(), d.dept.name(),
                    LEVELS[d.level], jobTitle(d), d.gender, d.hireDate, local, d.country.currency(),
                    fx.toUsd(local, d.country.currency())));
        }
        return out;
    }

    private static String jobTitle(Draft d) {
        return switch (d.level) {
            case 0 -> "Junior " + d.dept.role();
            case 1, 2 -> d.dept.role();
            case 3 -> "Senior " + d.dept.role();
            case 4 -> "Lead " + d.dept.role();
            case 5 -> "Director, " + d.dept.name();
            default -> "VP, " + d.dept.name();
        };
    }

    private static String uniqueEmail(Draft d, Set<String> taken) {
        String base = slug(d.first) + "." + slug(d.last);
        String email = base + "@acme.com";
        for (int n = 2; !taken.add(email); n++) {
            email = base + n + "@acme.com";
        }
        return email;
    }

    private static String slug(String s) {
        return s.toLowerCase().replace(" ", "");
    }

    private static <T> T pick(Random rnd, List<T> items, java.util.function.ToDoubleFunction<T> weight) {
        double r = rnd.nextDouble();
        double acc = 0;
        for (T item : items) {
            acc += weight.applyAsDouble(item);
            if (r < acc) {
                return item;
            }
        }
        return items.get(items.size() - 1);
    }

    private static int pickIndex(Random rnd, double[] weights) {
        double r = rnd.nextDouble();
        double acc = 0;
        for (int i = 0; i < weights.length; i++) {
            acc += weights[i];
            if (r < acc) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
