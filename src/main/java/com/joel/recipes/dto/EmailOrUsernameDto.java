package com.joel.recipes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailOrUsernameDto(
        @NotBlank
        @Size(min = 2, max = 150, message = "A valid username or email address is required")
        String usernameOrEmail
) {
}
