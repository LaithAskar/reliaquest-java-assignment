package com.challenge.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record EmployeeCreateRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Positive Integer salary,
        @NotNull @Min(16) @Max(120) Integer age,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotBlank @Email @Size(max = 254) String email,
        @NotNull @PastOrPresent Instant contractHireDate) {}
