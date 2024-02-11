package com.joel.recipes.exception;

public class EmailAddressAlreadyVerifiedException extends Exception {
    public EmailAddressAlreadyVerifiedException() {
        super("Email address is already verified");
    }
}
