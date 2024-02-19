package com.joel.recipes.service;

import com.joel.recipes.config.security.JWTTokenService;
import com.joel.recipes.exception.UserEntityDoesNotExistException;
import com.joel.recipes.model.AccountStatus;
import com.joel.recipes.model.AuthenticatedUserEntity;
import com.joel.recipes.model.UserEntity;
import com.joel.recipes.repository.RoleRepository;
import com.joel.recipes.repository.UserEntityRepository;
import com.joel.recipes.util.UserEntityMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        userEntity = UserEntity.builder()
                .id(id)
                .firstname("John")
                .lastname("Smith")
                .username("john123")
                .password("#Password123")
                .email("john@example.com")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
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
    void authenticateUserEntity() {
    }

    @Test
    void testAuthenticateUserEntity() {
    }

    @Test
    void registerNewUserEntity() {
    }

    @Test
    void emailVerificationRequest() {
    }

    @Test
    void verifyEmailAddressWithToken() {
    }

    @Test
    void loginUser() {
    }

    @Test
    void refreshToken() {
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
    void logoutUserEntity() {
    }

    @Test
    void testLogoutUserEntity() {
    }

    @Test
    void deleteUserEntity() {
    }

    @Test
    void loadUserByUsername() {
    }

    @Test
    void testAuthenticateUserEntity_withEmailAndPassword_userAuthenticated() throws UserEntityDoesNotExistException {

        // Arrange
        when(userEntityRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.ofNullable(userEntity));
       

        // Act and assert
        assertThat(userEntityService.authenticateUserEntity(userEntity.getEmail(), userEntity.getPassword())).isInstanceOf(AuthenticatedUserEntity.class);
    }

    @Test
    void testAuthenticateUserEntity2() {
    }

    @Test
    void testRegisterNewUserEntity() {
    }

    @Test
    void testEmailVerificationRequest() {
    }

    @Test
    void testVerifyEmailAddressWithToken() {
    }

    @Test
    void testLoginUser() {
    }

    @Test
    void testRefreshToken() {
    }

    @Test
    void testResetPasswordRequest() {
    }

    @Test
    void testResetPasswordWithToken() {
    }

    @Test
    void testApplyJsonPatchToUserEntity() {
    }

    @Test
    void testLogoutUserEntity1() {
    }

    @Test
    void testLogoutUserEntity2() {
    }

    @Test
    void testDeleteUserEntity() {
    }

    @Test
    void testLoadUserByUsername() {
    }
}