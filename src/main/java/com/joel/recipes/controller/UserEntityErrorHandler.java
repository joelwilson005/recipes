package com.joel.recipes.controller;

import com.joel.recipes.exception.EmailAddressAlreadyTakenException;
import com.joel.recipes.exception.UserEntityValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserEntityErrorHandler {
    @ExceptionHandler(EmailAddressAlreadyTakenException.class)
    public ProblemDetail accessDeniedHandler(EmailAddressAlreadyTakenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UserEntityValidationException.class)
    public ProblemDetail updateUserEntityValidationExceptionHandler(UserEntityValidationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setProperty("input_validation_errors", e.getErrors());
        return problemDetail;
    }
}
