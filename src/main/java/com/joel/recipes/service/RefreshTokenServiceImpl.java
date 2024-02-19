package com.joel.recipes.service;

import com.joel.recipes.exception.ExpiredRefreshTokenException;
import com.joel.recipes.exception.RefreshTokenNotFoundException;
import com.joel.recipes.exception.UserEntityDoesNotExistException;
import com.joel.recipes.model.RefreshToken;
import com.joel.recipes.model.UserEntity;
import com.joel.recipes.repository.RefreshTokenRepository;
import com.joel.recipes.repository.UserEntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${refresh-token-lifespan-in-days}")
    private int tokenLifespanInDays;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserEntityRepository userEntityRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserEntityRepository userEntityRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public RefreshToken createRefreshToken(String usernameOrEmail) throws UserEntityDoesNotExistException {
        UserEntity userEntity = this.userEntityRepository.findUserEntityByEmailOrUsername(usernameOrEmail).orElseThrow(UserEntityDoesNotExistException::new);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenValue(UUID.randomUUID())
                .expirationDate(Instant.now().plus(tokenLifespanInDays, ChronoUnit.DAYS))
                .userEntity(userEntity).build();
        refreshToken = this.refreshTokenRepository.save(refreshToken);
        userEntity.getRefreshTokens().add(refreshToken);
        this.userEntityRepository.save(userEntity);
        return refreshToken;
    }

    @Override
    public RefreshToken findRefreshTokenByTokenValue(String refreshTokenValue) throws RefreshTokenNotFoundException {
        return this.refreshTokenRepository.findByTokenValue(UUID.fromString(refreshTokenValue)).orElseThrow(RefreshTokenNotFoundException::new);
    }

    @Override
    public RefreshToken findRefreshTokenByTokenValue(UUID id) throws RefreshTokenNotFoundException {
        return this.refreshTokenRepository.findByTokenValue(id).orElseThrow(RefreshTokenNotFoundException::new);

    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken refreshToken) throws ExpiredRefreshTokenException {
        if (refreshToken.getExpirationDate().compareTo(Instant.now()) < 0) {
            this.refreshTokenRepository.delete(refreshToken);
            throw new ExpiredRefreshTokenException();
        }

        return refreshToken;
    }

    @Override
    public void deleteRefreshToken(String refreshTokenValue) throws RefreshTokenNotFoundException {
        RefreshToken refreshToken = this.findRefreshTokenByTokenValue(refreshTokenValue);
        this.refreshTokenRepository.deleteById(refreshToken.getId());
    }
}
