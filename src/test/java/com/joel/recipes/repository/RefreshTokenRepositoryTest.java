package com.joel.recipes.repository;

import com.joel.recipes.model.RefreshToken;
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
class RefreshTokenRepositoryTest {
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    RefreshToken refreshToken;

    final UUID ID = UUID.fromString("5381fce7-8b92-47ce-bbeb-fb7c37166f7f");
    final UUID TOKEN_VALUE = UUID.fromString("89ef100c-1d0f-4bb3-9295-4b5a6af31c9e");

    @BeforeEach
    void setUp() {
        refreshToken = RefreshToken.builder()
                .id(ID)
                .tokenValue(TOKEN_VALUE)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findByTokenValue_tokenFound() {
        // Arrange and act
        RefreshToken token = refreshTokenRepository.findByTokenValue(TOKEN_VALUE).orElseThrow();

        // Assert
        assertThat(token.getTokenValue()).isEqualTo(refreshToken.getTokenValue());
        assertThat(token.getId()).isEqualTo(refreshToken.getId());
    }

    @Test
    void findByTokenValue_tokenNotFound_exceptionThrown() {

        final UUID tokenNotInDb = UUID.randomUUID();

        // Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
            refreshTokenRepository.findByTokenValue(tokenNotInDb).orElseThrow();
        });
    }
}