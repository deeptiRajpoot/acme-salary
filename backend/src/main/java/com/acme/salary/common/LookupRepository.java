package com.acme.salary.common;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read access to the small reference tables (countries, departments, levels, FX rates). */
@Repository
public class LookupRepository {

    private final JdbcTemplate jdbc;

    public LookupRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Lookups.CountryInfo> countries() {
        return jdbc.query("SELECT code, name, currency FROM country ORDER BY name",
                (rs, i) -> new Lookups.CountryInfo(rs.getString(1), rs.getString(2), rs.getString(3)));
    }

    public List<String> departments() {
        return jdbc.queryForList("SELECT name FROM department ORDER BY name", String.class);
    }

    public List<String> levels() {
        return jdbc.queryForList("SELECT code FROM job_level ORDER BY sort_order", String.class);
    }

    public Map<String, BigDecimal> currencyRates() {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        jdbc.query("SELECT currency, rate_to_usd FROM currency_rate ORDER BY currency",
                rs -> {
                    rates.put(rs.getString(1), BigDecimal.valueOf(rs.getDouble(2)));
                });
        return rates;
    }
}
