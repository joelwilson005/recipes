package com.joel.recipes.exception;

public class EmailAddressNotVerifiedException extends Exception {
    public EmailAddressNotVerifiedException() {
        super("Email address is not yet verified");
    }
}
