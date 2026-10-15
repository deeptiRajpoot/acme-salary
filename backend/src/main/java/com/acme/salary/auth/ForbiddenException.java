package com.acme.salary.auth;

/** The caller is signed in but not allowed to do this (mapped to 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
