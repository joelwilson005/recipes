package com.joel.recipes.exception;

public class InvalidPasswordResetTokenException extends Exception {
    public InvalidPasswordResetTokenException() {
        super("Invalid password reset token");
    }
}
