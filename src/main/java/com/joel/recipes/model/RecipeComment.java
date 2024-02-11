package com.joel.recipes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeComment {
    @Id
    @GeneratedValue
    private UUID id;
    private Timestamp timeAdded;
    @Lob
    private String comment;
    @ManyToOne
    private Recipe recipe;
    @ManyToOne
    private UserEntity user;
}
