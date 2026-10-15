package com.acme.salary.employee;

import static com.acme.salary.support.TestData.input;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.ValidationException;
import com.acme.salary.salary.SalaryChangeInput;
import com.acme.salary.salary.SalaryHistoryEntry;
import com.acme.salary.support.IntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class EmployeeServiceTest {

    @Autowired
    EmployeeService service;

    @Test
    void createWritesInitialHistoryAndUsdSalary() {
        EmployeeResponse r = service.create(input("  Ada@Example.com "));
        assertThat(r.id()).isNotNull();
        assertThat(r.email()).isEqualTo("ada@example.com");
        assertThat(r.salaryUsd()).isEqualByComparingTo("100000");
        List<SalaryHistoryEntry> h = service.salaryHistory(r.id());
        assertThat(h).singleElement().satisfies(e -> {
            assertThat(e.oldSalary()).isNull();
            assertThat(e.newSalary()).isEqualByComparingTo("100000");
            assertThat(e.reason()).isEqualTo("Initial hire");
        });
    }

    @Test
    void createConvertsForeignCurrencyToUsd() {
        EmployeeResponse r = service.create(input("jp@example.com", "Sato", "JP", "Sales", "L2", "M", "9000000", "JPY"));
        assertThat(r.salaryUsd()).isEqualByComparingTo("60300.00");
    }

    @Test
    void validationReportsEveryBadField() {
        EmployeeInput bad = new EmployeeInput("", "X", "not-an-email", "ZZ", "Nope", "L9", "T", "Q",
                LocalDate.of(2030, 1, 1), new BigDecimal("-5"), "XXX");
        assertThatThrownBy(() -> service.create(bad)).isInstanceOfSatisfying(ValidationException.class,
                ex -> assertThat(ex.getErrors()).containsKeys("firstName", "email", "country", "department", "level",
                        "gender", "hireDate", "salary", "currency"));
    }

    @Test
    void duplicateEmailRejected() {
        service.create(input("dup@example.com"));
        assertThatThrownBy(() -> service.create(input("DUP@example.com"))).isInstanceOfSatisfying(
                ValidationException.class, ex -> assertThat(ex.getErrors()).containsKey("email"));
    }

    @Test
    void salaryChangeAppendsHistoryAndUpdatesUsd() {
        long id = service.create(input("raise@example.com")).id();
        EmployeeResponse r = service.changeSalary(id, new SalaryChangeInput(new BigDecimal("110000"), "Promotion", null));
        assertThat(r.salary()).isEqualByComparingTo("110000");
        assertThat(r.salaryUsd()).isEqualByComparingTo("110000");
        List<SalaryHistoryEntry> h = service.salaryHistory(id);
        assertThat(h).hasSize(2);
        assertThat(h.get(0).oldSalary()).isEqualByComparingTo("100000"); // newest first
        assertThat(h.get(0).newSalary()).isEqualByComparingTo("110000");
        assertThat(h.get(0).changedBy()).isEqualTo("hr.manager");
        assertThat(h.get(1).oldSalary()).isNull();
    }

    @Test
    void updateWithUnchangedSalaryEditsProfileWithoutHistory() {
        long id = service.create(input("edit@example.com")).id();
        EmployeeInput edit = new EmployeeInput("Grace", "Hopper", "edit@example.com", "US", "Product", "L4", "PM", "F",
                LocalDate.of(2020, 1, 15), new BigDecimal("100000.00"), "USD");
        EmployeeResponse r = service.update(id, edit);
        assertThat(r.firstName()).isEqualTo("Grace");
        assertThat(r.department()).isEqualTo("Product");
        assertThat(service.salaryHistory(id)).hasSize(1);
    }

    @Test
    void updateWithDifferentSalaryIsRejected() {
        long id = service.create(input("nochange@example.com")).id();
        EmployeeInput edit = input("nochange@example.com", "L", "US", "Engineering", "L3", "F", "120000", "USD");
        assertThatThrownBy(() -> service.update(id, edit)).isInstanceOfSatisfying(ValidationException.class,
                ex -> assertThat(ex.getErrors()).containsKey("salary"));
    }

    @Test
    void deleteRemovesEmployeeAndHistory() {
        long id = service.create(input("del@example.com")).id();
        service.delete(id);
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void missingEmployeeIs404Everywhere() {
        SalaryChangeInput change = new SalaryChangeInput(BigDecimal.TEN, "reason", null);
        assertThatThrownBy(() -> service.get(999_999)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.update(999_999, input("x@example.com"))).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(999_999)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.changeSalary(999_999, change)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.salaryHistory(999_999)).isInstanceOf(NotFoundException.class);
    }
}
