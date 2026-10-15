package com.acme.salary.analytics;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbc;

    public AnalyticsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** One narrow scan (~10k rows); percentiles/outliers are computed in Java from it. */
    public List<PayRow> findAllPayRows() {
        return jdbc.query(
                "SELECT id, first_name || ' ' || last_name, country, department, job_level, gender, salary_usd FROM employee",
                (rs, i) -> new PayRow(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getDouble(7)));
    }
}
