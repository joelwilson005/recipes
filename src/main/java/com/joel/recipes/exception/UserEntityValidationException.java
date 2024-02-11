package com.joel.recipes.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Setter
@Getter
public class UserEntityValidationException extends Exception {
    private HashMap<String, String> errors;

    public UserEntityValidationException(HashMap<String, String> errors) {
        super("Invalid input");
        this.errors = errors;
    }

}
