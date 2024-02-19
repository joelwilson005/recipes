package com.joel.recipes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LogoutUserEntityRequestDto(
        @NotBlank(message = "User ID cannot be blank") @Pattern(regexp = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$", message = "Invalid User ID format") String id,
        String refreshTokenValue) {

}
