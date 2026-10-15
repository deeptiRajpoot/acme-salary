package com.acme.salary.salary;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {

    List<SalaryHistory> findByEmployeeIdOrderByIdDesc(Long employeeId);

    long countByEmployeeId(Long employeeId);

    /** Only used when the employee itself is deleted. */
    @Modifying
    @Query("delete from SalaryHistory h where h.employeeId = :employeeId")
    void deleteByEmployeeId(Long employeeId);
}
