package com.joel.recipes.repository;

import com.joel.recipes.model.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage, UUID> {
}
