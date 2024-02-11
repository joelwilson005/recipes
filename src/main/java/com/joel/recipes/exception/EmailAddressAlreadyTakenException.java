package com.joel.recipes.exception;

public class EmailAddressAlreadyTakenException extends Exception {
    public EmailAddressAlreadyTakenException() {
        super("Email address is already taken");
    }
}
