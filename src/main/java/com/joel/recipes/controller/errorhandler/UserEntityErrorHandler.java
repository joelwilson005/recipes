package com.joel.recipes.controller.errorhandler;

import com.joel.recipes.exception.UserEntityDoesNotExistException;
import com.joel.recipes.exception.UserEntityValidationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(3)
public class UserEntityErrorHandler {
    @ExceptionHandler(UserEntityValidationException.class)
    public ProblemDetail updateUserEntityValidationExceptionHandler(UserEntityValidationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setProperty("validationErrors", e.getErrors());
        return problemDetail;
    }

    @ExceptionHandler(UserEntityDoesNotExistException.class)
    public ProblemDetail userEntityDoesNotExistHandler(UserEntityDoesNotExistException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
