package com.challenge.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmployeeModelTest {

    @Test
    void keepsFullNameSynchronizedWhenNamesChange() {
        EmployeeModel employee = new EmployeeModel(
                UUID.randomUUID(),
                "Avery",
                "Morgan",
                82_000,
                29,
                "Security Analyst",
                "avery.morgan@example.com",
                Instant.parse("2022-03-14T00:00:00Z"),
                null);

        employee.setFirstName("Jordan");
        assertThat(employee.getFullName()).isEqualTo("Jordan Morgan");

        employee.setLastName(null);
        assertThat(employee.getFullName()).isNull();
    }
}
