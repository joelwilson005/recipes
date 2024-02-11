package com.joel.recipes.exception;

public class InvalidEmailVerificationTokenException extends Exception {
    public InvalidEmailVerificationTokenException() {
        super("Email verification token is invalid");
    }
}
