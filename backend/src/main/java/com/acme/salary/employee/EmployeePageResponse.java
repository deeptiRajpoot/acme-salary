package com.acme.salary.employee;

import java.util.List;

public record EmployeePageResponse(List<EmployeeResponse> content, int page, int size, long totalElements,
                                   int totalPages) {
}
