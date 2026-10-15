package com.acme.salary.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

/** Pure local-currency -> USD conversion using static rates (1 unit of currency = rate USD). */
public final class FxConverter {

    private final Map<String, BigDecimal> ratesToUsd;

    public FxConverter(Map<String, BigDecimal> ratesToUsd) {
        this.ratesToUsd = Map.copyOf(ratesToUsd);
    }

    public BigDecimal toUsd(BigDecimal amount, String currency) {
        BigDecimal rate = ratesToUsd.get(currency);
        if (rate == null) {
            throw new IllegalArgumentException("Unknown currency: " + currency);
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean supports(String currency) {
        return ratesToUsd.containsKey(currency);
    }

    public Set<String> currencies() {
        return ratesToUsd.keySet();
    }

    public BigDecimal rate(String currency) {
        return ratesToUsd.get(currency);
    }
}
