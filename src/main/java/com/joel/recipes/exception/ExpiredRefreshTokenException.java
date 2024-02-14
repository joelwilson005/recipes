package com.joel.recipes.exception;

public class ExpiredRefreshTokenException extends Exception {
    public ExpiredRefreshTokenException() {
        super("Refresh token has expired");
    }
}
