package com.joel.recipes.service;

import com.joel.recipes.model.UserEntity;
import com.joel.recipes.model.VerificationToken;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    VerificationToken sendEmailVerificationToken(UserEntity userEntity) throws MessagingException, UnsupportedEncodingException;

    VerificationToken sendPasswordResetToken(UserEntity userEntity) throws MessagingException, UnsupportedEncodingException;
}
