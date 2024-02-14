package com.joel.recipes.service;

import com.joel.recipes.model.UserEntity;
import com.joel.recipes.model.VerificationToken;
import com.joel.recipes.util.VerificationTokenGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.logging.Level;

@Service
@Log
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String mailFrom;
    @Value("${mail.from.name}")
    private String mailFromName;

    private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";
    private static final String EMAIL_VERIFICATION_TEMPLATE_NAME = "verify-email";
    private static final String PASSWORD_RESET_TEMPLATE_NAME = "reset-password";
    private final JavaMailSender mailSender;
    private final TemplateEngine htmlTemplateEngine;
    private final UserEntityService userEntityService;


    private TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(emailTemplateResolver());
        return templateEngine;
    }

    private ITemplateResolver emailTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setResolvablePatterns(Collections.singleton("*"));
        templateResolver.setPrefix("/templates/email/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    public EmailServiceImpl(JavaMailSender mailSender, @Lazy UserEntityService userEntityService) {
        this.mailSender = mailSender;
        this.htmlTemplateEngine = emailTemplateEngine();
        this.userEntityService = userEntityService;
    }

    private enum TokenType {
        PASSWORD_RESET,
        EMAIL_VERIFICATION
    }

    @Override
    public VerificationToken sendEmailVerificationToken(UserEntity userEntity) throws MessagingException, UnsupportedEncodingException {
        return this.sendTokenEmail(userEntity, EMAIL_VERIFICATION_TEMPLATE_NAME, "Verify email", TokenType.EMAIL_VERIFICATION);
    }

    @Override
    public VerificationToken sendPasswordResetToken(UserEntity userEntity) throws MessagingException, UnsupportedEncodingException {
        return this.sendTokenEmail(userEntity, PASSWORD_RESET_TEMPLATE_NAME, "Reset password", TokenType.PASSWORD_RESET);
    }

    private VerificationToken sendTokenEmail(UserEntity userEntity, String templateName, String subject, TokenType tokenType) throws MessagingException, UnsupportedEncodingException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper email;

        email = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        email.setTo(userEntity.getEmail());
        email.setSubject(subject);
        email.setFrom(new InternetAddress(mailFrom, mailFromName));

        final var context = new Context(LocaleContextHolder.getLocale());

        String tokenString = VerificationTokenGenerator.generateToken();
        var tokenExpirationTime = LocalDateTime.now().plusMinutes(15L);
        var verificationToken = VerificationToken.builder()
                .verificationToken(VerificationTokenGenerator.generateToken())
                .expirationTimestamp(Timestamp.valueOf(tokenExpirationTime))
                .build();

        context.setVariable("token", tokenString);
        switch (tokenType) {
            case PASSWORD_RESET -> {
                verificationToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
                userEntity.setPasswordResetToken(verificationToken);
                this.userEntityService.updateUserEntity(userEntity);
            }
            case EMAIL_VERIFICATION -> {
                verificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
                userEntity.setEmailVerificationToken(verificationToken);
                this.userEntityService.updateUserEntity(userEntity);
            }
            default -> {
                log.log(Level.SEVERE, "Unable to send email - Unexpected email type");
                throw new IllegalStateException("Unexpected value: " + tokenType);
            }

        }

        final String htmlContent = this.htmlTemplateEngine.process(templateName, context);
        email.setText(htmlContent, true);
        mailSender.send(mimeMessage);
        return verificationToken;
    }
}
