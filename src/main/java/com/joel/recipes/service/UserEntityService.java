package com.joel.recipes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.joel.recipes.exception.*;
import com.joel.recipes.model.AuthenticatedUserEntity;
import com.joel.recipes.model.UserEntity;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
public interface UserEntityService extends UserDetailsService {
    UserEntity findUserEntityById(UUID id);

    void updateUserEntity(UserEntity userEntity);

    void registerNewUserEntity(UserEntity userEntity) throws Exception;

    AuthenticatedUserEntity verifyEmailAddressWithToken(String email, String password, String emailVerificationToken) throws Exception;

    AuthenticatedUserEntity authenticateUserEntity(String email, String password) throws UserEntityDoesNotExistException;

    AuthenticatedUserEntity loginUser(String email, String password) throws UserEntityDoesNotExistException, EmailAddressNotVerifiedException, EmailAddressAlreadyVerifiedException;

    void resetPasswordRequest(String email) throws UserEntityDoesNotExistException, MessagingException, UnsupportedEncodingException;

    String refreshToken(String refreshTokenValue) throws RefreshTokenNotFoundException, ExpiredVerificationTokenExeption, ExpiredRefreshTokenException;

    AuthenticatedUserEntity resetPasswordWithToken(String email, String verificationToken, String password) throws UserEntityDoesNotExistException, InvalidPasswordResetTokenException, ExpiredVerificationTokenExeption;

    void applyJsonPatchToUserEntity(JsonPatch patch, UUID id) throws UserEntityDoesNotExistException, JsonProcessingException, JsonPatchException, UsernameAlreadyTakenException, EmailAddressAlreadyTakenException, MessagingException, UnsupportedEncodingException, InvalidEmailAddressException, UserEntityValidationException;

    void logoutUserEntity(UUID id) throws UserEntityDoesNotExistException;

    void deleteUserEntity(UUID id) throws UserEntityDoesNotExistException;
}
