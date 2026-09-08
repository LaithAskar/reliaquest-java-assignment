package com.challenge.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.config.SecurityConfiguration;
import com.challenge.api.exception.ApiExceptionHandler;
import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeCreateRequest;
import com.challenge.api.model.EmployeeModel;
import com.challenge.api.service.EmployeeService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
@Import({ApiExceptionHandler.class, SecurityConfiguration.class})
@ActiveProfiles("test")
class EmployeeControllerTest {

    private static final UUID EMPLOYEE_UUID = UUID.fromString("33ed7516-a67c-391c-a9f9-cd36b5ed3e3d");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/employee").with(httpBasic("unknown-client", "invalid-value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee()));

        mockMvc.perform(get("/api/v1/employee").with(user("webhook-client")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").value(EMPLOYEE_UUID.toString()))
                .andExpect(jsonPath("$[0].fullName").value("Avery Morgan"));
    }

    @Test
    void returnsNotFoundForUnknownEmployee() throws Exception {
        when(employeeService.getEmployeeByUuid(EMPLOYEE_UUID)).thenThrow(new EmployeeNotFoundException(EMPLOYEE_UUID));

        mockMvc.perform(get("/api/v1/employee/{uuid}", EMPLOYEE_UUID).with(user("webhook-client")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsMalformedEmployeeUuid() throws Exception {
        mockMvc.perform(get("/api/v1/employee/not-a-uuid").with(user("webhook-client")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid path parameter"));
    }

    @Test
    void createsValidEmployee() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateRequest.class))).thenReturn(employee());

        mockMvc.perform(
                        post("/api/v1/employee")
                                .with(user("webhook-client"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "firstName": "Avery",
                                  "lastName": "Morgan",
                                  "salary": 82000,
                                  "age": 29,
                                  "jobTitle": "Security Analyst",
                                  "email": "avery.morgan@example.com",
                                  "contractHireDate": "2022-03-14T00:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/employee/" + EMPLOYEE_UUID))
                .andExpect(jsonPath("$.uuid").value(EMPLOYEE_UUID.toString()));
    }

    @Test
    void rejectsInvalidEmployee() throws Exception {
        mockMvc.perform(
                        post("/api/v1/employee")
                                .with(user("webhook-client"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "firstName": "",
                                  "lastName": "Morgan",
                                  "salary": -1,
                                  "age": 15,
                                  "jobTitle": "Analyst",
                                  "email": "not-an-email",
                                  "contractHireDate": "2022-03-14T00:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void rejectsOversizedAndFutureDatedEmployee() throws Exception {
        String oversizedFirstName = "A".repeat(101);

        mockMvc.perform(post("/api/v1/employee")
                        .with(user("webhook-client"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "firstName": "%s",
                                  "lastName": "Morgan",
                                  "salary": 82000,
                                  "age": 29,
                                  "jobTitle": "Security Analyst",
                                  "email": "avery.morgan@example.com",
                                  "contractHireDate": "2999-03-14T00:00:00Z"
                                }
                                """
                                        .formatted(oversizedFirstName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.contractHireDate").exists());
    }

    private Employee employee() {
        return new EmployeeModel(
                EMPLOYEE_UUID,
                "Avery",
                "Morgan",
                82_000,
                29,
                "Security Analyst",
                "avery.morgan@example.com",
                Instant.parse("2022-03-14T00:00:00Z"),
                null);
    }
}