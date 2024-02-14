package com.joel.recipes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.joel.recipes.config.security.JWTTokenService;
import com.joel.recipes.dto.UserEntityPatch;
import com.joel.recipes.exception.*;
import com.joel.recipes.model.*;
import com.joel.recipes.repository.RoleRepository;
import com.joel.recipes.repository.UserEntityRepository;
import com.joel.recipes.util.UserEntityMapper;
import jakarta.mail.MessagingException;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Log
public class UserEntityServiceImpl implements UserEntityService {
    private final UserEntityRepository userEntityRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenService jwtTokenService;
    private final EmailService emailService;
    private final UserEntityMapper userEntityMapper;
    private final RefreshTokenService refreshTokenService;

    public UserEntityServiceImpl(UserEntityRepository userEntityRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTTokenService jwtTokenService, EmailService emailService, UserEntityMapper userEntityMapper, RefreshTokenService refreshTokenService) {
        this.userEntityRepository = userEntityRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.emailService = emailService;
        this.userEntityMapper = userEntityMapper;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public UserEntity findUserEntityById(UUID id) {
        return this.userEntityRepository.findById(id).orElseThrow();
    }

    @Override
    public void updateUserEntity(UserEntity userEntity) {
        this.userEntityRepository.save(userEntity);
    }


    @Override
    public AuthenticatedUserEntity authenticateUserEntity(String email, String password) throws UserEntityDoesNotExistException {
        UserEntity userEntity = this.userEntityRepository.findUserEntityByEmail(email).orElseThrow(UserEntityDoesNotExistException::new);
        Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        String jwt = this.jwtTokenService.generateJwt(authentication, userEntity.getId());
        RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(email);
        return new AuthenticatedUserEntity(userEntity.getId(), jwt, refreshToken.getTokenValue().toString());
    }

    @Override
    public void registerNewUserEntity(UserEntity userEntity) throws Exception {
        var errorList = new HashMap<String, String>();

        if (this.userEntityRepository.findUserEntityByEmail(userEntity.getEmail()).isPresent()) {
            errorList.put("email", "Email address is already in use");
        }

        if (this.userEntityRepository.findUserEntityByUsername(userEntity.getUsername()).isPresent()) {
            errorList.put("username", "Username is already in use");
        }

        if (!errorList.isEmpty()) {
            throw new UserEntityValidationException(errorList);
        }

        Role userRole = roleRepository.findByAuthority(RoleType.USER.name()).orElseThrow(() -> new Exception("Role does not exist"));
        userEntity.setAuthorities(new HashSet<>(Collections.singletonList(userRole)));
        userEntity.setPassword(this.passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setAccountStatus(AccountStatus.ACTIVE);
        userEntity.setAccountCreationDate(Timestamp.from(Instant.now()));
        // Remove excess whitespace and capitalize
        userEntity.setFirstname(StringUtils.capitalize(StringUtils.trim(userEntity.getFirstname())));
        userEntity.setLastname(StringUtils.capitalize(StringUtils.trim(userEntity.getLastname())));
        this.emailService.sendEmailVerificationToken(userEntity);
        this.userEntityRepository.save(userEntity);
    }

    @Override
    public AuthenticatedUserEntity verifyEmailAddressWithToken(String email, String password, String emailVerificationToken) throws Exception {
        String trimmedToken = StringUtils.trim(emailVerificationToken);
        UserEntity userEntity = this.userEntityRepository.findUserEntityByEmail(email).orElseThrow(UserEntityDoesNotExistException::new);
        if (userEntity.isEmailVerified()) throw new EmailAddressAlreadyVerifiedException();

        if (userEntity.getEmailVerificationToken().getExpirationTimestamp().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ExpiredVerificationTokenExeption();
        }

        if (userEntity.getEmailVerificationToken().getVerificationToken().equals(trimmedToken)) {
            userEntity.setEmailVerified(true);
            userEntity.setEmailVerificationTimestamp(new Timestamp(System.currentTimeMillis()));
            userEntity.setEmailVerificationToken(null);
            this.userEntityRepository.save(userEntity);
            return this.authenticateUserEntity(userEntity.getEmail(), password);
        }

        throw new InvalidEmailVerificationTokenException();
    }


    @Override
    public AuthenticatedUserEntity loginUser(String userNameOrEmail, String password) throws UserEntityDoesNotExistException, EmailAddressAlreadyVerifiedException {
        UserEntity userEntity = this.userEntityRepository.findUserEntityByEmailOrUsername(userNameOrEmail).orElseThrow(UserEntityDoesNotExistException::new);
        if (!userEntity.isEmailVerified()) throw new EmailAddressAlreadyVerifiedException();
        String email = userEntity.getEmail();
        return this.authenticateUserEntity(email, password);
    }

    @Override
    public String refreshToken(String refreshTokenValue) throws RefreshTokenNotFoundException, ExpiredRefreshTokenException {
        RefreshToken refreshToken = this.refreshTokenService.findRefreshTokenByTokenValue(refreshTokenValue);
        this.refreshTokenService.verifyExpiration(refreshToken);
        UserEntity userEntity = refreshToken.getUserEntity();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null, userEntity.getAuthorities());
        return this.jwtTokenService.generateJwt(authentication, userEntity.getId());
    }


    @Override
    public void resetPasswordRequest(String usernameOrEmail) throws UserEntityDoesNotExistException, MessagingException, UnsupportedEncodingException {
        if (EmailValidator.getInstance().isValid(usernameOrEmail)) {
            this.emailService.sendPasswordResetToken(this.userEntityRepository.findUserEntityByEmail(usernameOrEmail).orElseThrow(UserEntityDoesNotExistException::new));
        } else {
            UserEntity userEntity = this.userEntityRepository.findUserEntityByUsername(usernameOrEmail).orElseThrow(UserEntityDoesNotExistException::new);
            this.emailService.sendPasswordResetToken(userEntity);
        }

    }

    @Override
    public AuthenticatedUserEntity resetPasswordWithToken(String usernameOrEmail, String verificationToken, String password) throws UserEntityDoesNotExistException, InvalidPasswordResetTokenException, ExpiredVerificationTokenExeption {
        UserEntity userEntity = this.userEntityRepository.findUserEntityByEmailOrUsername(usernameOrEmail).orElseThrow(UserEntityDoesNotExistException::new);
        if (userEntity.getPasswordResetToken().getExpirationTimestamp().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ExpiredVerificationTokenExeption();
        }

        if ((userEntity.getPasswordResetToken() != null) && (userEntity.getPasswordResetToken().getVerificationToken().equals(verificationToken))) {
            userEntity.setPassword(this.passwordEncoder.encode(password));
            userEntity.setPasswordResetToken(null);
            return this.authenticateUserEntity(userEntity.getEmail(), password);
        } else {
            throw new InvalidPasswordResetTokenException();
        }
    }

    @Override
    public void applyJsonPatchToUserEntity(JsonPatch patch, UUID id) throws UserEntityDoesNotExistException, JsonProcessingException, JsonPatchException, MessagingException, UnsupportedEncodingException, UserEntityValidationException {
        UserEntity userEntityToBeUpdated = this.userEntityRepository.findById(id).orElseThrow(UserEntityDoesNotExistException::new);

        ObjectMapper objectMapper = new ObjectMapper();
        UserEntityPatch userEntityPatch = new UserEntityPatch();

        JsonNode patchedUserEntity = patch.apply(objectMapper.convertValue(userEntityPatch, JsonNode.class));
        userEntityPatch = objectMapper.treeToValue(patchedUserEntity, UserEntityPatch.class);

        var errorList = new HashMap<String, String>();

        if (Objects.nonNull(userEntityPatch.getUsername())) {
            if (!Pattern.matches("^(?=.{4,20}$)(?:[a-zA-Z\\d]+(?:(?:\\.|-|_)[a-zA-Z\\d])*)+$", userEntityPatch.getUsername())) {
                errorList.put("username", "Invalid username");
            }
            if (!userEntityPatch.getUsername().isBlank()) {
                if (!userEntityPatch.getUsername().equals(userEntityToBeUpdated.getUsername())) {
                    if ((this.userEntityRepository.findUserEntityByUsername(userEntityPatch.getUsername()).isPresent())) {
                        errorList.put("username", "Username is is already in use");
                    }
                }
            }
        }

        boolean shouldEmailBeReverified = false;
        if (Objects.nonNull(userEntityPatch.getEmail())) {
            if (!EmailValidator.getInstance().isValid(userEntityPatch.getEmail())) {
                errorList.put("email", "Invalid email address");
            }
            if (!userEntityPatch.getEmail().isBlank()) {
                if (!userEntityPatch.getEmail().equals(userEntityToBeUpdated.getEmail())) {
                    if ((this.userEntityRepository.findUserEntityByEmail(userEntityPatch.getEmail()).isPresent())) {
                        errorList.put("email", "Email address is already in use");
                    }
                    shouldEmailBeReverified = true;
                }
            }
        }

        if (Objects.nonNull(userEntityPatch.getFirstname())) {
            if (!Pattern.matches("^[A-Za-z-']{1,50}(\\s[A-Za-z-']{1,50})?$", userEntityPatch.getFirstname())) {
                errorList.put("firstname", "Invalid firstname");
            }
        }

        if (Objects.nonNull(userEntityPatch.getLastname())) {
            if (!Pattern.matches("^[A-Za-z-']{1,50}(\\s[A-Za-z-']{1,50})?$", userEntityPatch.getLastname())) {
                errorList.put("lastname", "Invalid lastname");
            }
        }

        if (Objects.nonNull(userEntityPatch.getPassword())) {
            if (!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", userEntityPatch.getPassword())) {
                errorList.put("password", "Invalid password");
            } else {
                userEntityPatch.setPassword(this.passwordEncoder.encode(userEntityPatch.getPassword()));
            }
        }

        if (!errorList.isEmpty()) {
            throw new UserEntityValidationException(errorList);
        }

        // Map userEntityPatch to userEntity
        this.userEntityMapper.mapUserEntityPatchToUserEntity(userEntityPatch, userEntityToBeUpdated);

        // Mark userEntity's email as not being verified and send email verification token
        if (shouldEmailBeReverified) {
            userEntityToBeUpdated.setEmailVerified(false);
            this.emailService.sendEmailVerificationToken(userEntityToBeUpdated);
        }

        this.userEntityRepository.save(userEntityToBeUpdated);
    }

    @Override
    public void logoutUserEntity(UUID id) throws UserEntityDoesNotExistException {
        UserEntity userEntity = this.userEntityRepository.findById(id).orElseThrow(
                UserEntityDoesNotExistException::new
        );

        RefreshToken refreshToken;
        if (Objects.nonNull(userEntity.getRefreshToken())) {
            refreshToken = userEntity.getRefreshToken();
            this.refreshTokenService.deleteRefreshToken(refreshToken.getTokenValue().toString());
        }
    }

    @Override
    public void deleteUserEntity(UUID id) throws UserEntityDoesNotExistException {
        userEntityRepository.findById(id).orElseThrow(UserEntityDoesNotExistException::new);
        this.userEntityRepository.deleteById(id);
    }

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {

        Optional<UserEntity> userEntityOptional = userEntityRepository.findUserEntityByEmailOrUsername(usernameOrEmail);
        if (userEntityOptional.isPresent()) {
            return userEntityOptional.get();
        }
        throw new UserEntityDoesNotExistException();
    }
}