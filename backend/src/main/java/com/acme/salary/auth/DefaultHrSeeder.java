package com.acme.salary.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Ensures the default HR and Owner profiles exist on every start (overridable via acme.auth.hr.* / acme.auth.owner.*). */
@Component
public class DefaultHrSeeder implements ApplicationRunner {

    private final AuthService auth;
    private final String username;
    private final String password;
    private final String email;
    private final String ownerPassword;
    private final String ownerEmail;
    private final String ownerUsername;

    public DefaultHrSeeder(AuthService auth,
                           @Value("${acme.auth.hr.username:hr}") String username,
                           @Value("${acme.auth.hr.password:Hr@12345}") String password,
                           @Value("${acme.auth.hr.email:hr@acme.com}") String email,
                           @Value("${acme.auth.owner.username:owner}") String ownerUsername,
                           @Value("${acme.auth.owner.password:Acme@7878}") String ownerPassword,
                           @Value("${acme.auth.owner.email:owner@acme.com}") String ownerEmail) {
        this.ownerEmail = ownerEmail;
        this.ownerUsername = ownerUsername;
        this.ownerPassword = ownerPassword;
        this.auth = auth;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public void run(ApplicationArguments args) {
        auth.ensureUser("HR Manager", username, email, password, AuthService.ROLE_HR);
        auth.ensureUser("Owner", ownerUsername, ownerEmail, ownerPassword, AuthService.ROLE_OWNER);
    }
}
