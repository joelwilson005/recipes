package com.joel.recipes.repository;

import com.joel.recipes.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID>, CrudRepository<UserEntity, UUID> {
    Optional<UserEntity> findUserEntityByEmail(String email);

    Optional<UserEntity> findUserEntityByUsername(String username);

    default Optional<UserEntity> findUserEntityByEmailOrUsername(String emailOrUsername) {
        if (emailOrUsername.contains("@")) return findUserEntityByEmail(emailOrUsername);
        return findUserEntityByUsername(emailOrUsername);
    }
}
