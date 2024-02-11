package com.joel.recipes.exception;

public class UserEntityDoesNotExistException extends Exception {
    public UserEntityDoesNotExistException() {
        super("User does not exist");
    }
}
