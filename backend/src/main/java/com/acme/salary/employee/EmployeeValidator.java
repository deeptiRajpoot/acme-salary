package com.acme.salary.employee;

import com.acme.salary.common.LookupRepository;
import com.acme.salary.common.ValidationException;
import com.acme.salary.salary.FxConverter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Single validation path for API writes and CSV import rows: bean-validation constraints on
 * {@link EmployeeInput} first, then reference-data, date and uniqueness rules.
 */
@Component
public class EmployeeValidator {

    private final Validator validator;
    private final LookupRepository lookups;
    private final FxConverter fx;
    private final Clock clock;

    public EmployeeValidator(Validator validator, LookupRepository lookups, FxConverter fx, Clock clock) {
        this.validator = validator;
        this.lookups = lookups;
        this.fx = fx;
        this.clock = clock;
    }

    public ReferenceData referenceData() {
        return new ReferenceData(
                lookups.countries().stream().map(c -> c.code()).collect(Collectors.toSet()),
                Set.copyOf(lookups.departments()), Set.copyOf(lookups.levels()), Set.copyOf(fx.currencies()));
    }

    /**
     * @param input normalized input
     * @param emailTaken true when the email already belongs to another employee
     * @return field -> message, empty when valid
     */
    public Map<String, String> validate(EmployeeInput input, ReferenceData ref, Predicate<String> emailTaken) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<EmployeeInput> v : validator.validate(input)) {
            errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage());
        }
        requireKnown(errors, "country", input.country(), ref.countries());
        requireKnown(errors, "department", input.department(), ref.departments());
        requireKnown(errors, "level", input.level(), ref.levels());
        requireKnown(errors, "currency", input.currency(), ref.currencies());
        if (input.hireDate() != null && input.hireDate().isAfter(LocalDate.now(clock))) {
            errors.putIfAbsent("hireDate", "must not be in the future");
        }
        if (input.email() != null && !errors.containsKey("email") && emailTaken.test(input.email())) {
            errors.put("email", "is already in use");
        }
        return errors;
    }

    public void validateOrThrow(EmployeeInput input, ReferenceData ref, Predicate<String> emailTaken) {
        Map<String, String> errors = validate(input, ref, emailTaken);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static void requireKnown(Map<String, String> errors, String field, String value, Set<String> allowed) {
        if (value != null && !value.isBlank() && !allowed.contains(value)) {
            errors.putIfAbsent(field, "unknown value '" + value + "'");
        }
    }
}
