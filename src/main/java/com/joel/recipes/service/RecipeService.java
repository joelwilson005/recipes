package com.joel.recipes.service;

import com.joel.recipes.model.Recipe;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface RecipeService {
    Recipe addRecipe(Recipe recipe);
    Recipe updateRecipe(Recipe recipe);
    Recipe getRecipeById(UUID id);
    void deleteRecipeById(UUID id);
}
