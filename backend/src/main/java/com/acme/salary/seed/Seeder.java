package com.acme.salary.seed;

import com.acme.salary.salary.FxConverter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** Bulk-loads generated employees (+ initial-hire history rows) with JDBC batches in one transaction. */
@Service
public class Seeder {

    private static final Logger log = LoggerFactory.getLogger(Seeder.class);
    private static final int BATCH = 1000;
    private static final String INSERT_EMPLOYEE = "INSERT INTO employee (id, first_name, last_name, email, country, "
            + "department, job_level, job_title, gender, hire_date, salary, currency, salary_usd) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_HISTORY = "INSERT INTO salary_history (employee_id, old_salary, new_salary, "
            + "currency, reason, changed_by, changed_at) VALUES (?,NULL,?,?,?,?,?)";

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final FxConverter fx;
    private final long randomSeed;
    private final int count;
    private final LocalDate referenceDate;

    public Seeder(JdbcTemplate jdbc, TransactionTemplate tx, FxConverter fx,
                  @Value("${acme.seed.random-seed:42}") long randomSeed,
                  @Value("${acme.seed.count:10000}") int count,
                  @Value("${acme.seed.reference-date:2026-06-30}") String referenceDate) {
        this.jdbc = jdbc;
        this.tx = tx;
        this.fx = fx;
        this.randomSeed = randomSeed;
        this.count = count;
        this.referenceDate = LocalDate.parse(referenceDate);
    }

    /**
     * @param reset delete all existing employees and history first; otherwise do nothing if data exists
     * @return number of employees inserted (0 when skipped)
     */
    public int seed(boolean reset) {
        long start = System.nanoTime();
        List<SeedEmployee> employees = EmployeeGenerator.generate(randomSeed, count, referenceDate, fx);
        Integer inserted = tx.execute(status -> {
            if (reset) {
                jdbc.update("DELETE FROM salary_history");
                jdbc.update("DELETE FROM employee");
            } else if (jdbc.queryForObject("SELECT COUNT(*) FROM employee", Long.class) > 0) {
                return 0;
            }
            insert(employees);
            return employees.size();
        });
        log.info("Seeded {} employees in {} ms", inserted, (System.nanoTime() - start) / 1_000_000);
        return inserted == null ? 0 : inserted;
    }

    /** Employees get ids 1..N in generation order; history rows reference those ids. */
    private void insert(List<SeedEmployee> employees) {
        for (int from = 0; from < employees.size(); from += BATCH) {
            int offset = from;
            List<SeedEmployee> chunk = employees.subList(from, Math.min(from + BATCH, employees.size()));
            jdbc.batchUpdate(INSERT_EMPLOYEE, setter(chunk, (ps, e, idx) -> bindEmployee(ps, e, offset + idx + 1L)));
        }
        for (int from = 0; from < employees.size(); from += BATCH) {
            int offset = from;
            List<SeedEmployee> chunk = employees.subList(from, Math.min(from + BATCH, employees.size()));
            jdbc.batchUpdate(INSERT_HISTORY, setter(chunk, (ps, e, idx) -> bindHistory(ps, e, offset + idx + 1L)));
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps, SeedEmployee e, int index) throws SQLException;
    }

    private static BatchPreparedStatementSetter setter(List<SeedEmployee> chunk, Binder binder) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                binder.bind(ps, chunk.get(i), i);
            }

            @Override
            public int getBatchSize() {
                return chunk.size();
            }
        };
    }

    private static void bindEmployee(PreparedStatement ps, SeedEmployee e, long id) throws SQLException {
        int i = 1;
        ps.setLong(i++, id);
        ps.setString(i++, e.firstName());
        ps.setString(i++, e.lastName());
        ps.setString(i++, e.email());
        ps.setString(i++, e.country());
        ps.setString(i++, e.department());
        ps.setString(i++, e.level());
        ps.setString(i++, e.jobTitle());
        ps.setString(i++, e.gender());
        ps.setString(i++, e.hireDate().toString());
        ps.setDouble(i++, e.salary().doubleValue());
        ps.setString(i++, e.currency());
        ps.setDouble(i, e.salaryUsd().doubleValue());
    }

    private static void bindHistory(PreparedStatement ps, SeedEmployee e, long employeeId) throws SQLException {
        ps.setLong(1, employeeId);
        ps.setDouble(2, e.salary().doubleValue());
        ps.setString(3, e.currency());
        ps.setString(4, "Initial hire");
        ps.setString(5, "seed");
        ps.setString(6, e.hireDate().atTime(9, 0).toInstant(ZoneOffset.UTC).toString());
    }
}
