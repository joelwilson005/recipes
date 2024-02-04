package com.joel.recipes.controller;

import jakarta.validation.constraints.Email;

record CreateUserEntityRequestDto(
        String firstname,
        String lastname,
        @Email
        String email
) {
}
