package com.joel.recipes.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.joel.recipes.config.security.JWTTokenService;
import com.joel.recipes.exception.*;
import com.joel.recipes.model.*;
import com.joel.recipes.repository.RoleRepository;
import com.joel.recipes.repository.UserEntityRepository;
import com.joel.recipes.util.UserEntityMapper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserEntityServiceImplTest {

    @Mock
    UserEntityRepository userEntityRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JWTTokenService jwtTokenService;
    @Mock
    EmailService emailService;
    @Mock
    UserEntityMapper userEntityMapper;
    @Mock
    RefreshTokenService refreshTokenService;
    UserEntityService userEntityService;
    private AutoCloseable autoCloseable;
    final static UUID id = UUID.fromString("d70a888e-6b7d-434a-9063-4c8e7a2bf286");
    UserEntity userEntity;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);

        userEntityService = new UserEntityServiceImpl(userEntityRepository, roleRepository, passwordEncoder, authenticationManager, jwtTokenService, emailService, userEntityMapper, refreshTokenService);

        userEntity = UserEntity.builder().id(id).firstname("John").lastname("Smith").username("john123").password("#Password123").email("john@example.com").accountStatus(AccountStatus.ACTIVE).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void findUserEntityById_userEntityFound() {
        // Act and arrange
        when(userEntityRepository.findById(id)).thenReturn(Optional.ofNullable(userEntity));
        UserEntity userEntity1 = userEntityService.findUserEntityById(id);

        // Assert
        assertThat(userEntity1).isEqualTo(userEntity);
    }

    @Test
    void findUserEntityById_userEntityNotFound_exceptionThrown() {

        // Act and arrange
        when(userEntityRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        // Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            userEntityService.findUserEntityById(UUID.randomUUID());
        });
    }

    @Test
    void updateUserEntity() {

        // Arrange
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userEntityService.updateUserEntity(userEntity);

        // Assert
        verify(userEntityRepository).save(userEntity);
    }


    @Test
    void authenticateUserEntity_withEmailAndPassword_userEntityAuthenticated() throws UserEntityDoesNotExistException {

        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        String jwt = jwtTokenService.generateJwt(mockAuthentication, userEntity.getId());
        RefreshToken mockRefreshToken = mock(RefreshToken.class);
        final UUID refreshTokenValue = UUID.randomUUID();

        when(jwtTokenService.generateJwt(mockAuthentication, userEntity.getId())).thenReturn(jwt);
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEntity.getEmail(), userEntity.getPassword()))).thenReturn(mockAuthentication);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));
        when(refreshTokenService.createRefreshToken(userEntity.getEmail())).thenReturn(mockRefreshToken);
        when(mockRefreshToken.getTokenValue()).thenReturn(refreshTokenValue);

        AuthenticatedUserEntity authenticatedUserEntity = new AuthenticatedUserEntity(userEntity.getId(), jwt, refreshTokenValue.toString());


        // Act and assert
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isInstanceOf(AuthenticatedUserEntity.class);
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isEqualTo(authenticatedUserEntity);
    }

    @Test
    void authenticateUserEntity_withEmailAndPassword_userEntityNotFound_exceptionIsThrown() {

        // Arrange
        final String EMAIL_NOT_IN_DB = "emailnotindb@example.com";
        when(userEntityRepository.findUserEntityByEmail(EMAIL_NOT_IN_DB)).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.authenticateUserEntity(EMAIL_NOT_IN_DB, userEntity.getPassword());
        });
    }

    @Test
    void authenticateUserEntity_withEmailAndPassword_incorrectPassword_exceptionIsThrown() {

        // Arrange
        final String INCORRECT_PASSWORD = "#IncorrectPassword123";
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Password is incorrect"));

        // Act and assert
        assertThatExceptionOfType(BadCredentialsException.class).isThrownBy(() -> {
            userEntityService.authenticateUserEntity(userEntity.getEmail(), INCORRECT_PASSWORD);
        });

    }


    @Test
    void authenticateUserEntity_withEmailOnly_userEntityAuthenticated() throws UserEntityDoesNotExistException {

        // Arrange
        final UUID refreshTokenValue = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null, userEntity.getAuthorities());
        String jwt = jwtTokenService.generateJwt(authentication, userEntity.getId());
        RefreshToken mockRefreshToken = mock(RefreshToken.class);

        when(jwtTokenService.generateJwt(authentication, userEntity.getId())).thenReturn(jwt);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));
        when(refreshTokenService.createRefreshToken(userEntity.getEmail())).thenReturn(mockRefreshToken);
        when(mockRefreshToken.getTokenValue()).thenReturn(refreshTokenValue);

        AuthenticatedUserEntity authenticatedUserEntity = new AuthenticatedUserEntity(userEntity.getId(), jwt, refreshTokenValue.toString());

        // Act and assert
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isInstanceOf(AuthenticatedUserEntity.class);
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isEqualTo(authenticatedUserEntity);
    }

    @Test
    void authenticateUserEntity_withEmailOnly_userEntityNotFound_exceptionIsThrown() {

        // Arrange
        final String EMAIL_NOT_IN_DB = "emailnotindb@example.com";
        when(userEntityRepository.findUserEntityByEmail(EMAIL_NOT_IN_DB)).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.authenticateUserEntity(EMAIL_NOT_IN_DB);
        });
    }

    @Test
    void registerNewUserEntity_registrationSuccessful() throws Exception {

        // Arrange
        Role mockRole = mock(Role.class);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority(RoleType.USER.name())).thenReturn(Optional.ofNullable(mockRole));

        // Act
        userEntityService.registerNewUserEntity(userEntity);

        // Assert
        verify(userEntityRepository).save(userEntity);
        verify(emailService).sendEmailVerificationToken(userEntity);
    }

    @Test
    void registerNewUserEntity_emailAddressAlreadyInUse_exceptionThrown() {

        // Arrange
        Role mockRole = mock(Role.class);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.of(new UserEntity()));
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority(RoleType.USER.name())).thenReturn(Optional.ofNullable(mockRole));

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.registerNewUserEntity(userEntity);
        });
    }

    @Test
    void registerNewUserEntity_usernameAlreadyInUse_exceptionThrown() {
        // Arrange
        Role mockRole = mock(Role.class);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.of(new UserEntity()));
        when(roleRepository.findByAuthority(RoleType.USER.name())).thenReturn(Optional.ofNullable(mockRole));

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.registerNewUserEntity(userEntity);
        });
    }

    @Test
    void registerNewUserEntity_usernameAndEmailAlreadyInUse_exceptionThrown() {

        // Arrange
        Role mockRole = mock(Role.class);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.of(new UserEntity()));
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.of(new UserEntity()));
        when(roleRepository.findByAuthority(RoleType.USER.name())).thenReturn(Optional.ofNullable(mockRole));

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.registerNewUserEntity(userEntity);
        });
    }

    @Test
    void registerNewUserEntity_roleDoesNotExist_exceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority(RoleType.USER.name())).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            userEntityService.registerNewUserEntity(userEntity);
        });
    }

    @Test
    void emailVerificationRequest_verificationSent() throws UserEntityDoesNotExistException, MessagingException, UnsupportedEncodingException {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act
        userEntityService.emailVerificationRequest(userEntity.getEmail());

        // Assert
        verify(emailService).sendEmailVerificationToken(userEntity);

    }

    @Test
    void emailVerificationRequest_userEntityNotFound_exceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(userEntity.getEmail())).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.emailVerificationRequest(userEntity.getEmail());
        });
    }


    @Test
    void verifyEmailAddressWithToken_emailAddressVerified() throws Exception {

        // Arrange
        VerificationToken token = VerificationToken.builder().verificationToken("123456").tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION).expirationTimestamp(new Timestamp(System.currentTimeMillis() + 900000L)).build();
        userEntity.setEmailVerificationToken(token);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setTokenValue(UUID.randomUUID());
        when(refreshTokenService.createRefreshToken(userEntity.getEmail())).thenReturn(mockRefreshToken);

        // Act
        userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), token.getVerificationToken());

        // Assert
        verify(userEntityRepository).save(userEntity);
        assertThat(userEntity.isEmailVerified()).isTrue();
        assertThat(userEntity.getEmailVerificationTimestamp()).isNotNull();
        assertThat(userEntity.getEmailVerificationToken()).isNull();
    }

    @Test
    void verifyEmailAddressWithToken_userEntityNotFound_exceptionThrown() {

        // Arrange
        final String EMAIL_NOT_IN_DB = "nonexistentuser@example.com";
        when(userEntityRepository.findUserEntityByEmail(EMAIL_NOT_IN_DB)).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(EMAIL_NOT_IN_DB, any(String.class));
        });
    }

    @Test
    void verifyEmailAddresswithToken_emailAddressAlreadyVerified_exceptionThrown() {

        // Arrange
        userEntity.setEmailVerified(true);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(EmailAddressAlreadyVerifiedException.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), any(String.class));
        });
    }

    @Test
    void verifyEmailAddressWithToken_tokenIsExpired_exceptionThrown() {

        // Arrange
        VerificationToken token = VerificationToken.builder().verificationToken("123456").tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION).expirationTimestamp(new Timestamp(System.currentTimeMillis() - 900000L)).build();
        userEntity.setEmailVerificationToken(token);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(ExpiredVerificationTokenExeption.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), any(String.class));
        });
    }

    @Test
    void verifyEmailAddressWithToken_tokenIsInvalid_exceptionThrown() {

        // Arrange
        final String invalidTokenString = "654321";
        VerificationToken token = VerificationToken.builder().verificationToken("123456").tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION).expirationTimestamp(new Timestamp(System.currentTimeMillis() + 900000L)).build();
        userEntity.setEmailVerificationToken(token);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(InvalidEmailVerificationTokenException.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), invalidTokenString);
        });
    }


    @Test
    void loginUser_userLoggedInSuccessfully() throws UserEntityDoesNotExistException, EmailAddressAlreadyVerifiedException, EmailAddressNotVerifiedException {

        // Arrange
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setTokenValue(UUID.randomUUID());
        userEntity.setEmailVerified(true);
        when(refreshTokenService.createRefreshToken(userEntity.getEmail())).thenReturn(mockRefreshToken);
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));
        when(userEntityRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThat(userEntityService.loginUser(userEntity.getEmail(), userEntity.getPassword())).isInstanceOf(AuthenticatedUserEntity.class);
    }

    @Test
    void loginUser_userEntityNotFound_exceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.loginUser(userEntity.getEmail(), userEntity.getPassword());
        });
    }

    @Test
    void loginUser_emailNotVerified_exceptionThrown() {

        // Arrange
        userEntity.setEmailVerified(false);
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(EmailAddressNotVerifiedException.class).isThrownBy(() -> {
            userEntityService.loginUser(userEntity.getEmail(), userEntity.getPassword());
        });
    }

    @Test
    void loginUser_incorrectPassword_exceptionThrown() throws UserEntityDoesNotExistException {

        // Arrange
        final String INCORRECT_PASSWORD = "#Password321";
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setTokenValue(UUID.randomUUID());
        userEntity.setEmailVerified(true);
        when(refreshTokenService.createRefreshToken(userEntity.getEmail())).thenReturn(mockRefreshToken);
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));
        when(userEntityRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.ofNullable(userEntity));
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Incorrect password"));

        // Act and assert
        assertThatExceptionOfType(BadCredentialsException.class).isThrownBy(() -> {
            userEntityService.loginUser(userEntity.getEmail(), INCORRECT_PASSWORD);
        });
    }


    @Test
    void refreshToken_refreshTokenValid_newJwtReturned() throws RefreshTokenNotFoundException, ExpiredRefreshTokenException, ExpiredVerificationTokenExeption {

        // Arrange
        final String REFRESH_TOKEN_VALUE = "21c11823-4e5c-4eb0-8ea3-5e67e42c1220";
        final String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        RefreshToken refreshToken = RefreshToken.builder().tokenValue(UUID.fromString(REFRESH_TOKEN_VALUE)).expirationDate(Instant.now().plusSeconds(10_000L)).build();
        userEntity.setRefreshTokens(new HashSet<>());
        userEntity.getRefreshTokens().add(refreshToken);
        refreshToken.setUserEntity(userEntity);
        when(refreshTokenService.findRefreshTokenByTokenValue(REFRESH_TOKEN_VALUE)).thenReturn(refreshToken);
        when(jwtTokenService.generateJwt(any(Authentication.class), any(UUID.class))).thenReturn(mockJwt);


        // Act and assert
        assertThat(userEntityService.refreshToken(REFRESH_TOKEN_VALUE)).isInstanceOf(String.class);
        verify(jwtTokenService).generateJwt(any(Authentication.class), any(UUID.class));
    }

    @Test
    void refreshToken_refreshTokenNotFound_exceptionThrown() throws RefreshTokenNotFoundException {

        // Arrange
        final String REFRESH_TOKEN_VALUE = "21c11823-4e5c-4eb0-8ea3-5e67e42c1220";
        when(refreshTokenService.findRefreshTokenByTokenValue(any(String.class))).thenThrow(new RefreshTokenNotFoundException());

        // Act and assert
        assertThatExceptionOfType(RefreshTokenNotFoundException.class).isThrownBy(() -> {
            userEntityService.refreshToken(REFRESH_TOKEN_VALUE);
        });
    }

    @Test
    void refreshToken_refreshTokenExpired_exceptionThrown() throws RefreshTokenNotFoundException, ExpiredRefreshTokenException {

        // Arrange
        final String REFRESH_TOKEN_VALUE = "21c11823-4e5c-4eb0-8ea3-5e67e42c1220";
        RefreshToken refreshToken = RefreshToken.builder().tokenValue(UUID.fromString(REFRESH_TOKEN_VALUE)).expirationDate(Instant.now().minusSeconds(10_000L)).build();
        when(refreshTokenService.findRefreshTokenByTokenValue(REFRESH_TOKEN_VALUE)).thenReturn(refreshToken);
        when(refreshTokenService.verifyExpiration(refreshToken)).thenThrow(new ExpiredRefreshTokenException());

        // Act and assert
        assertThatExceptionOfType(ExpiredRefreshTokenException.class).isThrownBy(() -> {
            userEntityService.refreshToken(REFRESH_TOKEN_VALUE);
        });
    }

    @Test
    void resetPasswordRequest_emailProvided_emailSent() throws MessagingException, UnsupportedEncodingException, UserEntityDoesNotExistException {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act
        userEntityService.resetPasswordRequest(userEntity.getEmail());

        // Assert
        verify(emailService).sendPasswordResetToken(userEntity);
    }

    @Test
    void resetPasswordRequest_usernameProvided_emailSent() throws UserEntityDoesNotExistException, MessagingException, UnsupportedEncodingException {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(userEntity.getUsername())).thenReturn(Optional.ofNullable(userEntity));

        // Act
        userEntityService.resetPasswordRequest(userEntity.getUsername());

        // Assert
        verify(emailService).sendPasswordResetToken(userEntity);
    }

    @Test
    void resetPasswordRequest_userEntityDoesNotExist_exceptionThrown() {

        // Arrange
        final String NON_EXISTENT_EMAIL = "notexistentuser@example.com";
        when(userEntityRepository.findUserEntityByEmailOrUsername(NON_EXISTENT_EMAIL)).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.resetPasswordRequest(NON_EXISTENT_EMAIL);
        });
    }

    @Test
    void resetPasswordWithToken_tokenIsValid_passwordIsUpdated() throws InvalidPasswordResetTokenException, UserEntityDoesNotExistException, ExpiredVerificationTokenExeption {

        // Arrange
        final String VERIFICATION_TOKEN_VALUE = "123456";
        final String NEW_PASSWORD = "#Password123456";
        VerificationToken passwordResetToken = VerificationToken.builder().tokenType(VerificationToken.TokenType.PASSWORD_RESET).expirationTimestamp(Timestamp.from(Instant.now().plusSeconds(10_000L))).verificationToken(VERIFICATION_TOKEN_VALUE).build();
        userEntity.setPasswordResetToken(passwordResetToken);

        final String REFRESH_TOKEN_VALUE = "21c11823-4e5c-4eb0-8ea3-5e67e42c1220";
        RefreshToken refreshToken = RefreshToken.builder().tokenValue(UUID.fromString(REFRESH_TOKEN_VALUE)).expirationDate(Instant.now().plusSeconds(10_000L)).build();
        userEntity.setRefreshTokens(new HashSet<>());
        userEntity.getRefreshTokens().add(refreshToken);
        refreshToken.setUserEntity(userEntity);
        when(refreshTokenService.createRefreshToken(any(String.class))).thenReturn(refreshToken);
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));
        when(userEntityRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThat(userEntityService.resetPasswordWithToken(userEntity.getEmail(), VERIFICATION_TOKEN_VALUE, NEW_PASSWORD)).isInstanceOf(AuthenticatedUserEntity.class);
        assertThat(userEntity.getPasswordResetToken()).isNull();
        assertThat(userEntity.getPassword()).isNotEqualTo(NEW_PASSWORD);  // Password should be encoded
        assertThat(userEntity.getRefreshTokens().isEmpty()).isTrue();
    }

    @Test
    void resetPasswordWithToken_verificationTokenExpired_exceptionThrown() {

        // Arrange
        final String VERIFICATION_TOKEN_VALUE = "123456";
        final String NEW_PASSWORD = "#Password123456";
        VerificationToken passwordResetToken = VerificationToken.builder().tokenType(VerificationToken.TokenType.PASSWORD_RESET).expirationTimestamp(Timestamp.from(Instant.now().minusSeconds(10_000L))).verificationToken(VERIFICATION_TOKEN_VALUE).build();
        userEntity.setPasswordResetToken(passwordResetToken);
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(ExpiredVerificationTokenExeption.class).isThrownBy(() -> {
            userEntityService.resetPasswordWithToken(userEntity.getEmail(), VERIFICATION_TOKEN_VALUE, NEW_PASSWORD);
        });
    }

    @Test
    void resetPasswordWithToken_userEntityDoesNotExist_exceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.empty());
        final String NON_EXISTENT_USER = "nonexistentuser@example.com";
        final String VERIFICATION_TOKEN_VALUE = "123456";
        final String NEW_PASSWORD = "#Password123456";

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.resetPasswordWithToken(NON_EXISTENT_USER, VERIFICATION_TOKEN_VALUE, NEW_PASSWORD);
        });
    }

    @Test
    void resetPasswordWithToken_noTokenAttachedToUserEntity_exceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));
        userEntity.setPasswordResetToken(null);
        final String VERIFICATION_TOKEN_VALUE = "123456";
        final String NEW_PASSWORD = "#Password123456";

        // Act and assert
        assertThatExceptionOfType(InvalidPasswordResetTokenException.class).isThrownBy(() -> {
            userEntityService.resetPasswordWithToken(userEntity.getEmail(), VERIFICATION_TOKEN_VALUE, NEW_PASSWORD);
        });
    }

    @Test
    void applyJsonPatchToUserEntity() throws IOException, UserEntityDoesNotExistException, JsonPatchException, InvalidEmailAddressException, MessagingException, UsernameAlreadyTakenException, EmailAddressAlreadyTakenException, UserEntityValidationException {

        // Arrange
        String jsonPatchString = """
                [
                  { "op": "replace", "path": "/firstname", "value": "John" },
                  { "op": "replace", "path": "/lastname", "value": "Doe" },
                  { "op": "replace", "path": "/email", "value": "john@example.com" },
                  { "op": "replace", "path": "/username", "value": "johndoe123" },
                  { "op": "replace", "path": "/password", "value": "#Password123" }
                ]""";
        var updatedUserEntity = UserEntity.builder().firstname("John").lastname("Doe").email("john@example.com").username("johndoe123").password("#Password123").build();

        // Create an instance of ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // Convert the JSON string to a JsonNode object
        JsonNode jsonNode = mapper.readTree(jsonPatchString);
        JsonPatch patchObject = JsonPatch.fromJson(jsonNode);
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(userEntityRepository.findUserEntityByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.empty());
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(updatedUserEntity);


        // Act and assert
        assertThat(userEntityService.applyJsonPatchToUserEntity(patchObject, userEntity.getId())).isInstanceOf(UserEntity.class);
        assertThat(updatedUserEntity.getLastname()).isNotEqualTo(userEntity.getLastname());
    }

    @Test
    void applyJsonPatchTouserEntity_invalidUsername_exceptionThrown() throws IOException {

        // Arrange
        String jsonPatchString = """
                [
                  { "op": "replace", "path": "/firstname", "value": "John" },
                  { "op": "replace", "path": "/lastname", "value": "Doe" },
                  { "op": "replace", "path": "/email", "value": "john@example.com" },
                  { "op": "replace", "path": "/username", "value": "123" },
                  { "op": "replace", "path": "/password", "value": "#Password123" }
                ]"""; // username is invalid

        // Create an instance of ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // Convert the JSON string to a JsonNode object
        JsonNode jsonNode = mapper.readTree(jsonPatchString);
        JsonPatch patchObject = JsonPatch.fromJson(jsonNode);
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(userEntityRepository.findUserEntityByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.applyJsonPatchToUserEntity(patchObject, userEntity.getId());
        });
    }

    @Test
    void applyJsonPatchToUserEntity_userEntityNotFound_exceptionThrown() throws IOException {

        // Arrange
        String jsonPatchString = """
                [
                  { "op": "replace", "path": "/firstname", "value": "John" },
                  { "op": "replace", "path": "/lastname", "value": "Doe" },
                  { "op": "replace", "path": "/email", "value": "john@example.com" },
                  { "op": "replace", "path": "/username", "value": "johndoe123" },
                  { "op": "replace", "path": "/password", "value": "#Password123" }
                ]""";
        // Create an instance of ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // Convert the JSON string to a JsonNode object
        JsonNode jsonNode = mapper.readTree(jsonPatchString);
        JsonPatch patchObject = JsonPatch.fromJson(jsonNode);
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.applyJsonPatchToUserEntity(patchObject, userEntity.getId());
        });

    }

    @Test
    void applyJsonPatchTouserEntity_usernameAlreadyTaken_exceptionThrown() throws IOException {

        // Arrange
        String jsonPatchString = """
                [
                  { "op": "replace", "path": "/firstname", "value": "John" },
                  { "op": "replace", "path": "/lastname", "value": "Doe" },
                  { "op": "replace", "path": "/email", "value": "john@example.com" },
                  { "op": "replace", "path": "/username", "value": "123" },
                  { "op": "replace", "path": "/password", "value": "#Password123" }
                ]"""; // username is already taken

        // Create an instance of ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // Convert the JSON string to a JsonNode object
        JsonNode jsonNode = mapper.readTree(jsonPatchString);
        JsonPatch patchObject = JsonPatch.fromJson(jsonNode);
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(userEntityRepository.findUserEntityByUsername(any(String.class))).thenReturn(Optional.ofNullable(mock(UserEntity.class)));
        when(userEntityRepository.findUserEntityByEmail(any(String.class))).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.applyJsonPatchToUserEntity(patchObject, userEntity.getId());
        });

    }

    @Test
    void logoutUserEntity_fromAllDevices_userEntityLoggedOutSuccessfully() throws UserEntityDoesNotExistException, RefreshTokenNotFoundException {

        // Arrange
        final String REFRESH_TOKEN_VALUE = "21c11823-4e5c-4eb0-8ea3-5e67e42c1220";
        RefreshToken refreshToken = RefreshToken.builder().tokenValue(UUID.fromString(REFRESH_TOKEN_VALUE)).expirationDate(Instant.now().plusSeconds(10_000L)).build();
        userEntity.setRefreshTokens(new HashSet<>());
        userEntity.getRefreshTokens().add(refreshToken);
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act
        userEntityService.logoutUserEntity(userEntity.getId());

        // Assert
        assertThat(userEntity.getRefreshTokens()).isEmpty();
        verify(userEntityRepository, atLeastOnce()).save(userEntity);
    }

    @Test
    void logoutUserEntity_fromSingleDevice_userEntityLoggedOutSuccessfully() throws UserEntityDoesNotExistException, RefreshTokenNotFoundException {

        // Arrange
        final String REFRESH_TOKEN_VALUE = "21c11823-4e5c-4eb0-8ea3-5e67e42c1220";
        RefreshToken refreshToken = RefreshToken.builder().tokenValue(UUID.fromString(REFRESH_TOKEN_VALUE)).expirationDate(Instant.now().plusSeconds(10_000L)).build();
        userEntity.setRefreshTokens(new HashSet<>());
        userEntity.getRefreshTokens().add(refreshToken);
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(refreshTokenService.findRefreshTokenByTokenValue(any(UUID.class))).thenReturn(refreshToken);

        // Act
        userEntityService.logoutUserEntity(userEntity.getId());

        // Assert
        assertThat(userEntity.getRefreshTokens()).isEmpty();
        verify(userEntityRepository, atLeastOnce()).save(userEntity);
    }

    @Test
    void deleteUserEntity_userDeletedSuccessfully() throws UserEntityDoesNotExistException {

        // Arrange
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act
        userEntityService.deleteUserEntity(userEntity.getId());

        // Assert
        verify(userEntityRepository).deleteById(userEntity.getId());
    }

    @Test
    void deleteUserEntity_userEntityNotFound_exceptionThrown() throws UserEntityDoesNotExistException {

        // Arrange
        when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.deleteUserEntity(userEntity.getId());
        });
    }

    @Test
    void loadUserByUsername_userLoadedSuccessfully() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.ofNullable(userEntity));

        // Act
        var loadedUserEntity = userEntityService.loadUserByUsername(userEntity.getEmail());

        // Assert
        assertThat(loadedUserEntity).isEqualTo(userEntity);
    }

    @Test
    void loadUserByUsername_userEntityNotFound_exceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmailOrUsername(any(String.class))).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.loadUserByUsername(userEntity.getEmail());
        });
    }
}