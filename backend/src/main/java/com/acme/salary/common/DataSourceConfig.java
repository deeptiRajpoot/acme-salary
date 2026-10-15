package com.acme.salary.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    /**
     * SQLite file DB. WAL lets readers run during a write; busy_timeout avoids spurious
     * SQLITE_BUSY between pooled connections; foreign_keys enables ON DELETE CASCADE.
     */
    @Bean
    public DataSource dataSource(@Value("${acme.db.path}") String dbPath) throws IOException {
        Path path = Path.of(dbPath).toAbsolutePath();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:sqlite:" + path + "?journal_mode=WAL&busy_timeout=10000&foreign_keys=on&synchronous=NORMAL");
        cfg.setMaximumPoolSize(4);
        cfg.setPoolName("acme-sqlite");
        return new HikariDataSource(cfg);
    }
}
