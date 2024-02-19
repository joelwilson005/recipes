package com.joel.recipes.repository;

import com.joel.recipes.model.AccountStatus;
import com.joel.recipes.model.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
class UserEntityRepositoryTest {

    @Autowired
    UserEntityRepository userEntityRepository;
    UserEntity userEntity;

    final static UUID id = UUID.fromString("d70a888e-6b7d-434a-9063-4c8e7a2bf286");

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(id)
                .firstname("John")
                .lastname("Smith")
                .username("john123")
                .password("#Password123")
                .email("john@example.com")
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        userEntityRepository.save(userEntity);
    }

    @AfterEach
    void tearDown() {
        userEntity = null;
        userEntityRepository.deleteAll();
    }

    @Test
    void findUserEntityByEmail_userEntityFound() {
        // Arrange and act
        var userEntity1 = userEntityRepository.findUserEntityByEmail("john@example.com").orElseThrow();

        // Assert
        assertThat(userEntity1).isEqualTo(userEntity);
        assertThat(userEntity1.getId()).isEqualTo(userEntity.getId());
        assertThat(userEntity1.getEmail()).isEqualTo(userEntity.getEmail());
    }

    @Test
    void findUserEntityByEmail_userEntityNotFound_exceptionIsThrown() {
        // Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            userEntityRepository.findUserEntityByEmail("nonexistentuser@example.com").orElseThrow();
        });
    }

    @Test
    void findUserEntityByUsername_userEntityFound() {
        // Arrange and act
        var userEntity1 = userEntityRepository.findUserEntityByUsername("john123").orElseThrow();

        // Assert
        assertThat(userEntity1).isEqualTo(userEntity);
        assertThat(userEntity1.getId()).isEqualTo(userEntity.getId());
        assertThat(userEntity1.getUsername()).isEqualTo(userEntity.getUsername());

    }

    @Test
    void findUserEntityByUsername_userEntityNotFound_exceptionThrown() {
        // Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            userEntityRepository.findUserEntityByUsername("nonexistentuser123").orElseThrow();
        });
    }


    @Test
    void findUserEntityByEmailOrUsername_emailIsProvided_userEntityFound() {
        // Arrange and act
        var userEntity1 = userEntityRepository.findUserEntityByEmail("john@example.com").orElseThrow();

        // Assert
        assertThat(userEntity1).isEqualTo(userEntity);
        assertThat(userEntity1.getId()).isEqualTo(userEntity.getId());
        assertThat(userEntity1.getEmail()).isEqualTo(userEntity.getEmail());
    }

    @Test
    void findUserEntityByEmailOrUsername_usernameIsProvided_userEntityFound() {
        // Arrange and act
        var userEntity1 = userEntityRepository.findUserEntityByUsername("john123").orElseThrow();

        // Assert
        assertThat(userEntity1).isEqualTo(userEntity);
        assertThat(userEntity1.getId()).isEqualTo(userEntity.getId());
        assertThat(userEntity1.getUsername()).isEqualTo(userEntity.getUsername());
    }

    @Test
    void findUserEntityByEmailOrUsername_userEntityNotFound_exceptionThrown() {
        // Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            userEntityRepository.findUserEntityByEmailOrUsername("nonexistentuser123").orElseThrow();
        });

        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            userEntityRepository.findUserEntityByEmailOrUsername("nonexistentuser@example.com").orElseThrow();
        });
    }
}