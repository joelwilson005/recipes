package com.joel.recipes.controller.errorhandler;

import com.joel.recipes.util.ErrorReporter;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.UnsupportedEncodingException;

@RestControllerAdvice
public class EmailServiceErrorHandler {
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An internal server error has occurred";

    @ExceptionHandler(UnsupportedEncodingException.class)
    public ProblemDetail unsupportedEncodingHandler(UnsupportedEncodingException e) {
        ErrorReporter.reportError(e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
    }

    @ExceptionHandler(MessagingException.class)
    public ProblemDetail messagingExceptionHandler(MessagingException e) {
        ErrorReporter.reportError(e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
    }
}
