package com.joel.recipes.exception;

public class InvalidEmailAddressException extends Exception {
    public InvalidEmailAddressException() {
        super("Invalid email address");
    }
}
