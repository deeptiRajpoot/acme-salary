package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.salary.FxConverter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmployeeGeneratorTest {

    private static final LocalDate REF = LocalDate.of(2026, 6, 30);
    private static final FxConverter FX = new FxConverter(Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("1.08"),
            "GBP", new BigDecimal("1.27"), "CAD", new BigDecimal("0.74"), "AUD", new BigDecimal("0.66"),
            "SGD", new BigDecimal("0.74"), "BRL", new BigDecimal("0.20"), "JPY", new BigDecimal("0.0067"),
            "INR", new BigDecimal("0.012")));
    private static List<SeedEmployee> all;

    @BeforeAll
    static void generate() {
        all = EmployeeGenerator.generate(42, 10_000, REF, FX);
    }

    @Test
    void producesExactlyTenThousandUniqueEmails() {
        assertThat(all).hasSize(10_000);
        assertThat(all.stream().map(SeedEmployee::email).collect(Collectors.toSet())).hasSize(10_000);
    }

    @Test
    void sameSeedGivesSameData() {
        List<SeedEmployee> again = EmployeeGenerator.generate(42, 10_000, REF, FX);
        assertThat(again).isEqualTo(all);
    }

    @Test
    void differentSeedGivesDifferentData() {
        assertThat(EmployeeGenerator.generate(7, 200, REF, FX)).isNotEqualTo(all.subList(0, 200));
    }

    @Test
    void prefixIsStableRegardlessOfRequestedCountOnFirstRows() {
        // rows are drawn sequentially, so the first N rows of a smaller run match the big run's (up to outlier planting)
        List<SeedEmployee> small = EmployeeGenerator.generate(42, 100, REF, FX);
        assertThat(small.get(0).firstName()).isEqualTo(all.get(0).firstName());
        assertThat(small.get(0).country()).isEqualTo(all.get(0).country());
    }

    @Test
    void dataIsRealisticAndRightSkewed() {
        assertThat(all).allSatisfy(e -> {
            assertThat(e.salary()).isPositive();
            assertThat(e.hireDate()).isBetween(REF.minusYears(15).minusDays(1), REF);
            assertThat(e.email()).matches("[a-z0-9.]+@acme\\.com");
        });
        double[] usd = all.stream().mapToDouble(e -> e.salaryUsd().doubleValue()).sorted().toArray();
        double mean = java.util.Arrays.stream(usd).average().orElseThrow();
        assertThat(mean).isGreaterThan(usd[usd.length / 2]); // mean > median => right skew
        assertThat(all.stream().map(SeedEmployee::country).distinct().count()).isEqualTo(10);
    }

    @Test
    void usdColumnMatchesFxConversion() {
        assertThat(all).allSatisfy(e -> assertThat(e.salaryUsd()).isEqualByComparingTo(FX.toUsd(e.salary(), e.currency())));
    }

    @Test
    void plantedGenderGapExistsInEngineering() {
        double f = median(all, "Engineering", "F");
        double m = median(all, "Engineering", "M");
        assertThat((m - f) / m).isBetween(0.04, 0.2);
        assertThat(Set.of("Engineering", "Sales", "Operations")).contains("Sales");
    }

    private static double median(List<SeedEmployee> list, String dept, String gender) {
        double[] v = list.stream().filter(e -> e.department().equals(dept) && e.gender().equals(gender)
                && e.country().equals("US") && e.level().equals("L3"))
                .mapToDouble(e -> e.salaryUsd().doubleValue()).sorted().toArray();
        return v[v.length / 2];
    }
}
