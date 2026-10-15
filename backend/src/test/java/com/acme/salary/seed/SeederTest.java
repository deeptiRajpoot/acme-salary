package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class SeederTest {

    @Autowired
    Seeder seeder;
    @Autowired
    JdbcTemplate jdbc;

    private long count(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }

    @Test
    void seedsExactlyTenThousandWithHistoryAndIsIdempotent() {
        assertThat(seeder.seed(false)).isEqualTo(10_000);
        assertThat(count("SELECT COUNT(*) FROM employee")).isEqualTo(10_000);
        assertThat(count("SELECT COUNT(DISTINCT email) FROM employee")).isEqualTo(10_000);
        assertThat(count("SELECT COUNT(*) FROM salary_history")).isEqualTo(10_000);
        assertThat(count("SELECT COUNT(*) FROM employee WHERE country NOT IN (SELECT code FROM country)")).isZero();
        assertThat(count("SELECT COUNT(*) FROM employee WHERE department NOT IN (SELECT name FROM department)")).isZero();
        assertThat(seeder.seed(false)).isZero(); // table not empty -> no-op
        assertThat(count("SELECT COUNT(*) FROM employee")).isEqualTo(10_000);
    }

    @Test
    void resetReplacesDataWithSameContent() {
        seeder.seed(false);
        String first = jdbc.queryForObject("SELECT email || salary_usd FROM employee WHERE id = 1", String.class);
        assertThat(seeder.seed(true)).isEqualTo(10_000);
        assertThat(jdbc.queryForObject("SELECT email || salary_usd FROM employee WHERE id = 1", String.class))
                .isEqualTo(first);
    }
}
