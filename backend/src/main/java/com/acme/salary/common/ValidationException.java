package com.acme.salary.common;

import java.util.Map;

/** Business validation failure carrying a field -> message map (rendered as problem `errors`). */
public class ValidationException extends RuntimeException {
    private final transient Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public ValidationException(String field, String message) {
        this(Map.of(field, message));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
