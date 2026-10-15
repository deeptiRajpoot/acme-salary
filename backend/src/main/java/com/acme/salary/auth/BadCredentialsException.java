package com.acme.salary.auth;

/** Sign-in failed or the bearer token is missing/expired (mapped to 401). */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
