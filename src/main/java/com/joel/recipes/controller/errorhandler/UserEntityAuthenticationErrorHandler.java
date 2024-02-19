package com.joel.recipes.controller.errorhandler;

import com.joel.recipes.exception.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(1)
public class UserEntityAuthenticationErrorHandler {
    @ExceptionHandler(EmailAddressAlreadyTakenException.class)
    public ProblemDetail emailAddressAlreadyTakenHandler(EmailAddressAlreadyTakenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(EmailAddressAlreadyVerifiedException.class)
    public ProblemDetail emailAddressAlreadyTakenHandler(EmailAddressAlreadyVerifiedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.OK, e.getMessage());

    }

    @ExceptionHandler(EmailAddressNotVerifiedException.class)
    public ProblemDetail emailAddressNotVerifiedHandler(EmailAddressNotVerifiedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ProblemDetail expiredRefreshTokenHandler(ExpiredRefreshTokenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ExpiredVerificationTokenExeption.class)
    public ProblemDetail expiredVerificationTokenHandler(ExpiredVerificationTokenExeption e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());

    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ProblemDetail refreshTokenNotFoundHandler(RefreshTokenNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(InvalidEmailVerificationTokenException.class)
    public ProblemDetail invalidEmailAddressVerificationTokenHandler(InvalidEmailVerificationTokenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());

    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ProblemDetail invalidPasswordResetTokenHandler(InvalidPasswordResetTokenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());

    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ProblemDetail usernameAlreadyTakenHandler(UsernameAlreadyTakenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail badCredentialsHandler(BadCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Unauthorized - Bad credentials");
    }
}
