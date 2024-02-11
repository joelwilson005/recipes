package com.joel.recipes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequestDto(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email address cannot be blank")
        String email,
          /*
        - At least 8 characters
        - Contains at least one digit
        - Contains at least one lower-case letter and one upper-case letter
        -Contains at least one character within a set of special character (@#%$^ etc.)
        - May contain whitespace
         */
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", message = "Invalid password format")
        @NotBlank(message = "Password cannot be empty")
        String password,
        @NotBlank(message = "Email verification token cannot be blank")
        @Pattern(regexp = "\\d{6}", message = "Verification token must contain 6 digits")
        String emailVerificationToken) {
}
