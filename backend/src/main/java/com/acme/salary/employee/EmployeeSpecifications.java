package com.acme.salary.employee;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeFilter f) {
        return (root, query, cb) -> {
            List<Predicate> and = new ArrayList<>();
            if (hasText(f.q())) {
                String like = "%" + escapeLike(f.q().trim().toLowerCase()) + "%";
                and.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like, '\\'),
                        cb.like(cb.lower(root.get("lastName")), like, '\\'),
                        cb.like(cb.lower(root.get("email")), like, '\\'),
                        cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                                like, '\\')));
            }
            if (hasText(f.country())) {
                and.add(cb.equal(root.get("country"), f.country().trim().toUpperCase()));
            }
            if (hasText(f.department())) {
                and.add(cb.equal(root.get("department"), f.department().trim()));
            }
            if (hasText(f.level())) {
                and.add(cb.equal(root.get("level"), f.level().trim().toUpperCase()));
            }
            return cb.and(and.toArray(new Predicate[0]));
        };
    }

    static Specification<Employee> idAfter(long id) {
        return (root, query, cb) -> cb.greaterThan(root.get("id"), id);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
