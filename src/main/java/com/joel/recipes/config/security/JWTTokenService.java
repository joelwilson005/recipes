package com.joel.recipes.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JWTTokenService {
    private final JwtEncoder jwtEncoder;

    @Autowired
    public JWTTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateJwt(Authentication auth, UUID userId) {

        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(""));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .subject(auth.getName())
                .claim("roles", scope)
                .claim("id", userId)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();
        // Encoding the JWT and returning the token value
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}