package com.acme.salary.employee;

import static com.acme.salary.support.TestData.input;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.common.ValidationException;
import com.acme.salary.support.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class EmployeeSearchTest {

    private static final EmployeeFilter NONE = new EmployeeFilter(null, null, null, null);

    @Autowired
    EmployeeService service;

    @BeforeEach
    void data() {
        service.create(input("a@x.com", "Adams", "US", "Engineering", "L3", "F", "100000", "USD"));
        service.create(input("b@x.com", "Baker", "US", "Sales", "L3", "M", "90000", "USD"));
        service.create(input("c@x.com", "Clark", "IN", "Engineering", "L2", "M", "2000000", "INR"));
        service.create(input("d@x.com", "50%_Off", "GB", "Finance", "L5", "F", "80000", "GBP"));
        service.create(input("e@x.com", "Evans", "US", "Engineering", "L4", "X", "130000", "USD"));
    }

    @Test
    void pagesWithTotals() {
        EmployeePageResponse p = service.search(NONE, 1, 2, "lastName,asc");
        assertThat(p.totalElements()).isEqualTo(5);
        assertThat(p.totalPages()).isEqualTo(3);
        assertThat(p.page()).isEqualTo(1);
        assertThat(p.content()).extracting(EmployeeResponse::lastName).containsExactly("Baker", "Clark");
    }

    @Test
    void filtersCombine() {
        assertThat(names(new EmployeeFilter(null, "us", "Engineering", null))).containsExactly("Adams", "Evans");
        assertThat(names(new EmployeeFilter(null, null, null, "l2"))).containsExactly("Clark");
    }

    @Test
    void searchMatchesNameAndEmailCaseInsensitively() {
        assertThat(names(new EmployeeFilter("ADAMS", null, null, null))).containsExactly("Adams");
        assertThat(names(new EmployeeFilter("pat adams", null, null, null))).containsExactly("Adams");
        assertThat(names(new EmployeeFilter("c@x", null, null, null))).containsExactly("Clark");
    }

    @Test
    void likeWildcardsAreLiteral() {
        assertThat(names(new EmployeeFilter("50%_", null, null, null))).containsExactly("50%_Off");
        assertThat(names(new EmployeeFilter("%", null, null, null))).containsExactly("50%_Off");
    }

    @Test
    void sortsBySalaryUsdDescending() {
        List<EmployeeResponse> c = service.search(NONE, 0, 10, "salaryUsd,desc").content();
        assertThat(c).extracting(EmployeeResponse::lastName).startsWith("Evans", "50%_Off", "Adams");
    }

    @Test
    void rejectsBadSortAndSize() {
        assertThatThrownBy(() -> service.search(NONE, 0, 10, "password,asc")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.search(NONE, 0, 10, "lastName,sideways")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.search(NONE, 0, 201, "lastName,asc")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.search(NONE, -1, 10, "lastName,asc")).isInstanceOf(ValidationException.class);
    }

    private List<String> names(EmployeeFilter f) {
        return service.search(f, 0, 50, "lastName,asc").content().stream().map(EmployeeResponse::lastName).toList();
    }
}
