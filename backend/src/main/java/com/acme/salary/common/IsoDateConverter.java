package com.acme.salary.common;

import jakarta.persistence.AttributeConverter;
import java.time.LocalDate;

/** Stores LocalDate as ISO text (sorts correctly in SQLite, identical to the JDBC seeder's format). */
public class IsoDateConverter implements AttributeConverter<LocalDate, String> {
    @Override
    public String convertToDatabaseColumn(LocalDate value) {
        return value == null ? null : value.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
