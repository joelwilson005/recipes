package com.joel.recipes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationToken {
    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @Id
    @GeneratedValue
    private UUID id;
    private String verificationToken;
    private Timestamp expirationTimestamp;
    private TokenType tokenType;
}
