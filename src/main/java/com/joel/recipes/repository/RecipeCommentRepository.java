package com.joel.recipes.repository;

import com.joel.recipes.model.RecipeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecipeCommentRepository extends JpaRepository<RecipeComment, UUID> {
}
