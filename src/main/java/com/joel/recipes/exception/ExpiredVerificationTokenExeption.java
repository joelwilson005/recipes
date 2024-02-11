package com.joel.recipes.exception;

public class ExpiredVerificationTokenExeption extends Exception {
    public ExpiredVerificationTokenExeption() {
        super("Verification token has expired");
    }
}
