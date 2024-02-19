package com.joel.recipes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequestDto(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email address cannot be blank")
        String email,
        @NotBlank(message = "Email verification token cannot be blank")
        @Pattern(regexp = "\\d{6}", message = "Verification token must contain 6 digits")
        String emailVerificationToken) {
}
