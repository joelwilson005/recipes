package com.joel.recipes.controller;

import com.joel.recipes.dto.*;
import com.joel.recipes.exception.*;
import com.joel.recipes.model.AuthenticatedUserEntity;
import com.joel.recipes.model.UserEntity;
import com.joel.recipes.service.UserEntityService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.UUID;


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
        return new ResponseEntity<>(this.userEntityService.verifyEmailAddressWithToken(request.email(), request.emailVerificationToken()), HttpStatus.OK);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserEntity> login(@Valid @RequestBody LoginRequestDto request) throws UserEntityDoesNotExistException, EmailAddressNotVerifiedException, EmailAddressAlreadyVerifiedException {
        return new ResponseEntity<>(this.userEntityService.loginUser(request.email(), request.password()), HttpStatus.OK);
    }

    @PostMapping("/verify-email-request")
    public ResponseEntity<ApiMessage> verifyEmail(@Valid @RequestBody EmailOrUsernameDto requestDto) throws UserEntityDoesNotExistException, MessagingException, UnsupportedEncodingException {
        this.userEntityService.emailVerificationRequest(requestDto.usernameOrEmail());
        return new ResponseEntity<>(new ApiMessage("Email sent successfully"), HttpStatus.OK);
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
        var authenticatedUserEntity = this.userEntityService.resetPasswordWithToken(request.usernameOrEmail(), request.passwordResetToken(), request.password());
        return new ResponseEntity<>(authenticatedUserEntity, HttpStatus.OK);
    }

    /*
        Refresh tokens are sent to this endpoint to obtain new JWTs
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<JWTResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) throws ExpiredVerificationTokenExeption, RefreshTokenNotFoundException, ExpiredRefreshTokenException {
        String refreshToken = this.userEntityService.refreshToken(requestDto.refreshToken());
        JWTResponseDto responseDto = new JWTResponseDto(refreshToken);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiMessage> logoutUser(@Valid @RequestBody LogoutUserEntityRequestDto requestDto) throws UserEntityDoesNotExistException, RefreshTokenNotFoundException {
        this.userEntityService.logoutUserEntity(UUID.fromString(requestDto.id()), UUID.fromString(requestDto.refreshTokenValue()));
        return new ResponseEntity<>(new ApiMessage("User logged out successfully"), HttpStatus.OK);
    }

    @PostMapping("/logout/all")
    public ResponseEntity<ApiMessage> logoutUserFromAll(@Valid @RequestBody LogoutUserEntityRequestDto requestDto) throws UserEntityDoesNotExistException, RefreshTokenNotFoundException {
        this.userEntityService.logoutUserEntity(UUID.fromString(requestDto.id()));
        return new ResponseEntity<>(new ApiMessage("User logged out successfully"), HttpStatus.OK);
    }

    //todo remember to delete this method
    @GetMapping(value = "/restricted-url", consumes = MediaType.ALL_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> restrictedUrl() {
        return new ResponseEntity<>("You have access to the restricted url", HttpStatus.OK);
    }
}