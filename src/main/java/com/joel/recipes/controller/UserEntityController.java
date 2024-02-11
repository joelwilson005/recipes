package com.joel.recipes.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.joel.recipes.dto.ApiMessage;
import com.joel.recipes.exception.*;
import com.joel.recipes.service.UserEntityService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
@RequestMapping(value = "${api}" + "user", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserEntityController {
    private final UserEntityService userEntityService;

    public UserEntityController(UserEntityService userEntityService) {
        this.userEntityService = userEntityService;
    }

    @PatchMapping(value = "/{userId}", consumes = "application/json-patch+json")
    public ResponseEntity<ApiMessage> updateUserEntity(@PathVariable UUID userId, @RequestBody JsonPatch patch) throws JsonPatchException, InvalidEmailAddressException, MessagingException, UnsupportedEncodingException, UserEntityDoesNotExistException, JsonProcessingException, UsernameAlreadyTakenException, EmailAddressAlreadyTakenException, UserEntityValidationException {
        this.userEntityService.applyJsonPatchToUserEntity(patch, userId);
        return new ResponseEntity<>(new ApiMessage("User successfully updated"), HttpStatus.OK);
    }
}
