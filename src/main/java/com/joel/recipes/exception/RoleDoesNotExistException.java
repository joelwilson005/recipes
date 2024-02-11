package com.joel.recipes.exception;

public class RoleDoesNotExistException extends Exception {
    public RoleDoesNotExistException() {
        super("Role does not exist");
    }
}
