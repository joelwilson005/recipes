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
public class RecipeImage {
    @Id
    @GeneratedValue
    private UUID id;
    @OneToOne
    private Recipe recipe;
    @OneToOne
    private UserEntity user;
    @Lob
    private String url;
    private Timestamp timeAdded;
}
