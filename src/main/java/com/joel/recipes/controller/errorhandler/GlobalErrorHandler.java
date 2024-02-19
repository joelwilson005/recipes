package com.joel.recipes.controller.errorhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import com.joel.recipes.exception.RoleDoesNotExistException;
import com.joel.recipes.util.ErrorReporter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(100)
public class GlobalErrorHandler {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An internal server error has occurred";


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperty("validationErrors", errors);
        return problemDetail;
    }

    @ExceptionHandler(RoleDoesNotExistException.class)
    public ProblemDetail roleDoesNotExistHandler(RoleDoesNotExistException e) {
        ErrorReporter.reportError(e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ProblemDetail jsonProcessingHandler(JsonProcessingException e) {
        ErrorReporter.reportError(e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
    }

    @ExceptionHandler(JsonPatchException.class)
    public ProblemDetail jsonPatchHandler(JsonPatchException e) {
        ErrorReporter.reportError(e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail mediaTypeNotSupportedHandler(HttpMediaTypeNotSupportedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "HTTP media type is not supported");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail globalExceptionHandler(Exception e) {
        ErrorReporter.reportError(e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
    }

}
