package com.challenge.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeCreateRequest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryEmployeeServiceTest {

    private final InMemoryEmployeeService service = new InMemoryEmployeeService();

    @Test
    void returnsSeedEmployeesInStableOrder() {
        var employees = service.getAllEmployees();

        assertThat(employees).hasSize(2).isSortedAccordingTo((left, right) -> left.getUuid()
                .compareTo(right.getUuid()));
        assertThatThrownBy(employees::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void createsAndRetrievesEmployee() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "  Taylor ",
                " Smith  ",
                75_000,
                25,
                "  Analyst ",
                "  taylor.smith@example.com ",
                Instant.parse("2024-01-15T00:00:00Z"));

        Employee created = service.createEmployee(request);

        assertThat(created.getUuid()).isNotNull();
        assertThat(created.getFullName()).isEqualTo("Taylor Smith");
        assertThat(service.getEmployeeByUuid(created.getUuid())).isSameAs(created);
        assertThat(service.getAllEmployees()).hasSize(3);
    }

    @Test
    void rejectsUnknownEmployee() {
        UUID unknownUuid = UUID.randomUUID();

        assertThatThrownBy(() -> service.getEmployeeByUuid(unknownUuid))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining(unknownUuid.toString());
    }
}
