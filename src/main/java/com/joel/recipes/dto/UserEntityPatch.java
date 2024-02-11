package com.joel.recipes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    This class is used to apply JSON patches to UserEntity objects.
    It is used to prevent modification of certain fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntityPatch {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
}
