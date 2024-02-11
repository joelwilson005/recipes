package com.joel.recipes.controller;

import com.joel.recipes.dto.*;
import com.joel.recipes.exception.EmailAddressNotVerifiedException;
import com.joel.recipes.exception.ExpiredVerificationTokenExeption;
import com.joel.recipes.exception.InvalidPasswordResetTokenException;
import com.joel.recipes.exception.UserEntityDoesNotExistException;
import com.joel.recipes.model.AuthenticatedUserEntity;
import com.joel.recipes.model.UserEntity;
import com.joel.recipes.service.UserEntityService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;


@RestController
@RequestMapping(value = "${api}" + "user/auth", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserEntityAuthenticationController {

    private final UserEntityService userEntityService;

    public UserEntityAuthenticationController(UserEntityService userEntityService) {
        this.userEntityService = userEntityService;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiMessage> registerUser(@Valid @RequestBody RegisterUserEntityRequestDto request) throws Exception {
        var userEntity = UserEntity.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .username(request.username())
                .password(request.password())
                .build();
        this.userEntityService.registerNewUserEntity(userEntity);
        return new ResponseEntity<>(new ApiMessage("User successfully created"), HttpStatus.CREATED);
    }

    @PostMapping("/register/verify-email")
    public ResponseEntity<AuthenticatedUserEntity> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) throws Exception {
        return new ResponseEntity<>(this.userEntityService.verifyEmailAddressWithToken(
                request.email(), request.password(), request.emailVerificationToken()
        ), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserEntity> login(@Valid @RequestBody LoginRequestDto request) throws UserEntityDoesNotExistException, EmailAddressNotVerifiedException {
        return new ResponseEntity<>(this.userEntityService.loginUser(
                request.email(), request.password()), HttpStatus.OK);
    }

    /*
        This endpoint allows users to request password reset tokens to be
        sent to their email address
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<ApiMessage> resetPassword(@Valid @RequestBody EmailOrUsernameDto request) throws MessagingException, UnsupportedEncodingException, UserEntityDoesNotExistException {
        this.userEntityService.resetPasswordRequest(request.usernameOrEmail());
        return new ResponseEntity<>(new ApiMessage("Email sent successfully"), HttpStatus.OK);
    }

    /*
        After a user has requested a password reset token,
        they send their username/email, new password and the token to this endpoint
     */
    @PostMapping("/reset-password-token")
    public ResponseEntity<AuthenticatedUserEntity> resetPasswordToken(@Valid @RequestBody ResetPasswordDto request) throws InvalidPasswordResetTokenException, UserEntityDoesNotExistException, ExpiredVerificationTokenExeption {
        var authenticatedUserEntity = this.userEntityService.resetPasswordWithToken(
                request.usernameOrEmail(),
                request.passwordResetToken(),
                request.password()
        );
        return new ResponseEntity<>(authenticatedUserEntity, HttpStatus.OK);
    }
}