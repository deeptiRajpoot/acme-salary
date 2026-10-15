package com.acme.salary.common;

import jakarta.persistence.AttributeConverter;
import java.time.Instant;

/** Stores Instant as ISO-8601 text. */
public class IsoInstantConverter implements AttributeConverter<Instant, String> {
    @Override
    public String convertToDatabaseColumn(Instant value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String value) {
        return value == null ? null : Instant.parse(value);
    }
}
