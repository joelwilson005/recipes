package com.joel.recipes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterUserEntityRequestDto(
        @NotBlank(message = "Firstname cannot be blank")
        @Pattern(regexp = "^[A-Za-z-']{1,50}(\\s[A-Za-z-']{1,50})?$", message = "Invalid firstname")
        String firstname,
        @NotBlank(message = "Lastname cannot be blank")
        @Pattern(regexp = "^[A-Za-z-']{1,50}(\\s[A-Za-z-']{1,50})?$", message = "Invalid lastname")
        String lastname,
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email address cannot be blank")
        String email,

        /*
         * Rules for a valid username:
         * 1. The username must be between 4 and 20 characters in length.
         * 2. It must start and end with an alphanumeric character.
         * 3. It can include additional alphanumeric characters,
         *    and sequences of dot (.), hyphen (-), or underscore (_) followed by alphanumeric characters.
         */

        @Pattern(regexp = "^(?=.{4,20}$)(?:[a-zA-Z\\d]+(?:(?:\\.|-|_)[a-zA-Z\\d])*)+$", message = "Invalid username")
        @NotBlank(message = "Username cannot be blank")
        String username,
        /*
         * Password requirements:
         * - At least 8 characters
         * - Contains at least one digit
         * - Contains at least one lower-case letter and one upper-case letter
         * - Contains at least one special character from the set (@#%$^, etc.)
         * - May contain whitespace
         */
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", message = "Invalid password format")
        @NotBlank(message = "Password cannot be blank")
        String password) {
}
