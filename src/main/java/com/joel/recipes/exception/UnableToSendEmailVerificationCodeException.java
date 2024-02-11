package com.joel.recipes.exception;

public class UnableToSendEmailVerificationCodeException extends Exception {
    public UnableToSendEmailVerificationCodeException() {
        super("Unable to send email verification token");
    }
}
