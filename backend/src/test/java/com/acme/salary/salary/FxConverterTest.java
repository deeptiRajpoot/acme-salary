package com.acme.salary.salary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FxConverterTest {

    private final FxConverter fx = new FxConverter(Map.of("USD", BigDecimal.ONE, "JPY", new BigDecimal("0.0067"),
            "GBP", new BigDecimal("1.27")));

    @Test
    void convertsAndRoundsHalfUpToCents() {
        assertThat(fx.toUsd(new BigDecimal("100000"), "GBP")).isEqualByComparingTo("127000.00");
        assertThat(fx.toUsd(new BigDecimal("7500000"), "JPY")).isEqualByComparingTo("50250.00");
        assertThat(fx.toUsd(new BigDecimal("0.125"), "GBP")).isEqualByComparingTo("0.16"); // 0.15875
    }

    @Test
    void usdIsIdentity() {
        assertThat(fx.toUsd(new BigDecimal("123.45"), "USD")).isEqualByComparingTo("123.45");
    }

    @Test
    void unknownCurrencyFails() {
        assertThat(fx.supports("XYZ")).isFalse();
        assertThatThrownBy(() -> fx.toUsd(BigDecimal.ONE, "XYZ")).isInstanceOf(IllegalArgumentException.class);
    }
}
