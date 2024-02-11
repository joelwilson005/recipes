package com.joel.recipes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Recipe {
    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    @Lob
    private String description;
    @ElementCollection
    private Set<String> ingredients;
    @Lob
    private String directions;
    private Timestamp timeAdded;
    @ManyToOne
    private UserEntity creator;
    private String timeRequired;
    @ElementCollection
    private Set<String> tags;
    @OneToOne
    private RecipeImage image;
    @ManyToMany
    private Set<UserEntity> viewers;
    @ManyToMany
    private Set<UserEntity> favourites;
    @OneToMany
    private Set<RecipeComment> comments;
}
