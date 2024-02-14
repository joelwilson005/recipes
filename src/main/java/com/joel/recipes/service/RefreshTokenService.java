package com.joel.recipes.service;

import com.joel.recipes.exception.ExpiredRefreshTokenException;
import com.joel.recipes.exception.RefreshTokenNotFoundException;
import com.joel.recipes.exception.UserEntityDoesNotExistException;
import com.joel.recipes.model.RefreshToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface RefreshTokenService {
    RefreshToken createRefreshToken(String usernameOrEmail) throws UserEntityDoesNotExistException;

    RefreshToken findRefreshTokenByTokenValue(String refreshTokenValue) throws RefreshTokenNotFoundException;

    RefreshToken findRefreshTokenByTokenValue(UUID id) throws RefreshTokenNotFoundException;

    RefreshToken verifyExpiration(RefreshToken refreshToken) throws ExpiredRefreshTokenException;

    void deleteRefreshToken(String refreshTokenValue);

}
