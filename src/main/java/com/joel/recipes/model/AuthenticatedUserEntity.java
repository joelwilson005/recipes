package com.joel.recipes.model;

import java.util.UUID;

public record AuthenticatedUserEntity(UUID id, String jwt) {
}
