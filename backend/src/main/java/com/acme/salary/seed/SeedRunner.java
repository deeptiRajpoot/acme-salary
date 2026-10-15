package com.acme.salary.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * `--seed` seeds (add `--reset` to wipe first) and, because main() disables the web server for it, exits.
 * Otherwise seeds on startup only when acme.seed.enabled=true and the table is empty.
 */
@Component
public class SeedRunner implements ApplicationRunner {

    private final Seeder seeder;
    private final boolean enabled;

    public SeedRunner(Seeder seeder, @Value("${acme.seed.enabled:false}") boolean enabled) {
        this.seeder = seeder;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("seed")) {
            seeder.seed(args.containsOption("reset"));
        } else if (enabled) {
            seeder.seed(false);
        }
    }
}
