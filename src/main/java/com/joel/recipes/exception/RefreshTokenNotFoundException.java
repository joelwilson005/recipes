package com.joel.recipes.exception;

public class RefreshTokenNotFoundException extends Exception {
    public RefreshTokenNotFoundException() {
        super("Unable to find refresh token");
    }
}
