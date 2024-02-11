package com.joel.recipes.repository;

import com.joel.recipes.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TokenRepository extends JpaRepository<VerificationToken, UUID>, CrudRepository<VerificationToken, UUID> {
}
