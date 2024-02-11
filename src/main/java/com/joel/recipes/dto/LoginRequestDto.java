package com.joel.recipes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDto(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email address cannot be blank")
        String email,
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", message = "Invalid password format")
        @NotBlank(message = "Password cannot be empty")
        String password) {
}
