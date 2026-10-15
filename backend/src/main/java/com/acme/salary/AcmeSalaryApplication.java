package com.acme.salary;

import java.util.Arrays;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AcmeSalaryApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AcmeSalaryApplication.class);
        // `--seed` is a one-shot command: no web server, exits when the seeder finishes.
        if (Arrays.asList(args).contains("--seed")) {
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }
}
