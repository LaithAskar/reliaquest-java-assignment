package com.challenge.api.service;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeCreateRequest;
import com.challenge.api.model.EmployeeModel;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class InMemoryEmployeeService implements EmployeeService {

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public InMemoryEmployeeService() {
        addSeedEmployee(
                "Avery",
                "Morgan",
                82_000,
                29,
                "Security Analyst",
                "avery.morgan@example.com",
                Instant.parse("2022-03-14T00:00:00Z"));
        addSeedEmployee(
                "Jordan",
                "Lee",
                96_000,
                34,
                "Software Engineer",
                "jordan.lee@example.com",
                Instant.parse("2020-08-03T00:00:00Z"));
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employees.values().stream()
                .sorted(Comparator.comparing(Employee::getUuid))
                .toList();
    }

    @Override
    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employees.get(uuid);
        if (employee == null) {
            throw new EmployeeNotFoundException(uuid);
        }
        return employee;
    }

    @Override
    public Employee createEmployee(EmployeeCreateRequest request) {
        UUID uuid = UUID.randomUUID();
        Employee employee = new EmployeeModel(
                uuid,
                request.firstName().trim(),
                request.lastName().trim(),
                request.salary(),
                request.age(),
                request.jobTitle().trim(),
                request.email().trim(),
                request.contractHireDate(),
                null);
        employees.put(uuid, employee);
        return employee;
    }

    private void addSeedEmployee(
            String firstName,
            String lastName,
            Integer salary,
            Integer age,
            String jobTitle,
            String email,
            Instant contractHireDate) {
        UUID uuid = UUID.nameUUIDFromBytes(email.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        employees.put(
                uuid,
                new EmployeeModel(uuid, firstName, lastName, salary, age, jobTitle, email, contractHireDate, null));
    }
}
