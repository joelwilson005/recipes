package com.joel.recipes.service;

import com.joel.recipes.config.security.JWTTokenService;
import com.joel.recipes.exception.*;
import com.joel.recipes.model.*;
import com.joel.recipes.repository.RoleRepository;
import com.joel.recipes.repository.UserEntityRepository;
import com.joel.recipes.util.UserEntityMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
    UserEntityService userEntityService;
    AutoCloseable autoCloseable;

    UserEntity userEntity;

    final static UUID userEntityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        userEntityService = new UserEntityServiceImpl(userEntityRepository, roleRepository, passwordEncoder, authenticationManager, jwtTokenService, emailService, userEntityMapper);
        userEntity = UserEntity.builder().id(userEntityId).firstname("John").username("john123").lastname("Smith").email("johnsmith@example.com").password("#Password123").build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void test_findUserEntityById_userEntityFound() {

        // Arrange
        when(userEntityRepository.findById(userEntityId)).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThat(userEntityService.findUserEntityById(userEntityId)).isEqualTo(userEntity);
    }

    @Test
    void test_findUserEntityById_userEntityNotFound_thenExceptionThrown() {

        // Arrange
        when(userEntityRepository.findById(any(UUID.class))).thenThrow(new NoSuchElementException());

        // Act and assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            userEntityService.findUserEntityById(any(UUID.class));
        });
    }

    @Test
    void test_updateUserEntity_successful() {

        // Arrange
        when(userEntityRepository.save(userEntity)).thenReturn(userEntity);

        // Act
        userEntityService.updateUserEntity(userEntity);

        // Assert
        verify(userEntityRepository).save(userEntity);
    }

    @Test
    void test_authenticateUserEntity_successful() throws UserEntityDoesNotExistException {

        // Arrange
        UsernamePasswordAuthenticationToken mockToken = mock(UsernamePasswordAuthenticationToken.class);
        String jwt = jwtTokenService.generateJwt(mockToken, userEntityId);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));
        when(authenticationManager.authenticate(mockToken)).thenReturn(mockToken);
        when(jwtTokenService.generateJwt(mockToken, userEntityId)).thenReturn(jwt);

        // Act and assert
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isEqualTo(new AuthenticatedUserEntity(userEntityId, jwt));
    }

    @Test
    void test_authenticateUser_userEntityIsNotFound_thenExceptionIsThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail("email@example")).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityDoesNotExistException.class).isThrownBy(() -> {
            userEntityService.authenticateUserEntity("email@example.com", any(String.class));
        });
    }

    @Test
    void test_authenticateUser_passwordIsIncorrect_thenExceptionIsThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail("validemail@example.com")).thenReturn(Optional.ofNullable(userEntity));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act and assert
        assertThatExceptionOfType(BadCredentialsException.class).isThrownBy(() -> {
            userEntityService.authenticateUserEntity("validemail@example.com", "invalidPassword#123");
        });
    }

    @Test
    void test_registerNewUserEntity_successful() throws Exception {

        // Arrange
        Role mockRole = mock(Role.class);
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority(RoleType.USER.name())).thenReturn(Optional.ofNullable(mockRole));

        // Act
        userEntityService.registerNewUserEntity(userEntity);

        // Assert
        verify(emailService).sendEmailVerificationToken(userEntity);
        verify(userEntityRepository).save(userEntity);
    }

    @Test
    void test_registerNewUserEntity_emailIsAlreadyTaken_thenExceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.of(new UserEntity()));
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.empty());

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.registerNewUserEntity(userEntity);
        });

    }

    @Test
    void test_registerNewUserEntity_usernameIsAlreadyTaken_thenExceptionThrown() throws Exception {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.empty());
        when(userEntityRepository.findUserEntityByUsername(userEntity.getUsername())).thenReturn(Optional.of(new UserEntity()));

        // Act and assert
        assertThatExceptionOfType(UserEntityValidationException.class).isThrownBy(() -> {
            userEntityService.registerNewUserEntity(userEntity);
        });
    }

    @Test
    void test_verifyEmailAddressWithToken_success() throws Exception {

        // Arrange
        VerificationToken token = VerificationToken.builder()
                .verificationToken("123456")
                .tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expirationTimestamp(new Timestamp(System.currentTimeMillis() + 900000L))
                .build();
        userEntity.setEmailVerificationToken(token);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act
        userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), userEntity.getPassword(), userEntity.getEmailVerificationToken().getVerificationToken());

        // Assert
        verify(userEntityRepository).save(userEntity);
        assertThat(userEntity.isEmailVerified()).isTrue();
        assertThat(userEntity.getEmailVerificationTimestamp()).isNotNull();
        assertThat(userEntity.getEmailVerificationToken()).isNull();
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isInstanceOf(AuthenticatedUserEntity.class);
    }

    @Test
    void test_verifyEmailAddressWithToken_EmailAlreadyVerified_ExceptionThrown() {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));
        userEntity.setEmailVerified(true);

        // Act and assert
        assertThatExceptionOfType(EmailAddressAlreadyVerifiedException.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), userEntity.getPassword(), "123456");
        });
    }

    @Test
    void test_verifyEmailAddressWithToken_TokenExpired_ExceptionThrown() {

        // Arrange
        VerificationToken token = VerificationToken.builder()
                .verificationToken("123456")
                .tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expirationTimestamp(new Timestamp(System.currentTimeMillis() - 900000L))
                .build();
        userEntity.setEmailVerificationToken(token);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(ExpiredVerificationTokenExeption.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), userEntity.getPassword(), "123456");
        });
    }

    @Test
    void test_verifyEmailAddressWithToken_TokenInvalid_ExceptionIsThrown() {

        // Arrange
        VerificationToken token = VerificationToken.builder()
                .verificationToken("123456")
                .tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expirationTimestamp(new Timestamp(System.currentTimeMillis() + 900000L))
                .build();
        userEntity.setEmailVerificationToken(token);
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));

        // Act and assert
        assertThatExceptionOfType(InvalidEmailVerificationTokenException.class).isThrownBy(() -> {
            userEntityService.verifyEmailAddressWithToken(userEntity.getEmail(), userEntity.getPassword(), "654321");
        });
    }


    @Test
    void loginUser() {
    }

    @Test
    void resetPasswordRequest() {
    }

    @Test
    void resetPasswordWithToken() {
    }

    @Test
    void applyJsonPatchToUserEntity() {
    }

    @Test
    void deleteUserEntity() {
    }

    @Test
    void loadUserByUsername() {
    }
}