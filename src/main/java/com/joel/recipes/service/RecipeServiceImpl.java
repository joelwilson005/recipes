package com.joel.recipes.service;

import com.joel.recipes.model.Recipe;
import com.joel.recipes.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RecipeServiceImpl implements RecipeService{
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    public Recipe addRecipe(Recipe recipe) {
        return this.recipeRepository.save(recipe);
    }

    @Override
    public Recipe updateRecipe(Recipe recipe) {
        return this.recipeRepository.save(recipe);
    }

    @Override
    public Recipe getRecipeById(UUID id) {
        return this.recipeRepository.findById(id).orElseThrow();
    }

    @Override
    public void deleteRecipeById(UUID id) {
        this.recipeRepository.deleteById(id);
    }
}
