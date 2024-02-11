package com.joel.recipes.exception;

public class UsernameAlreadyTakenException extends Exception {
    public UsernameAlreadyTakenException() {
        super("Username is already taken");
    }
}
