package com.joel.recipes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue
    private UUID id;
    private String authority;

    public Role() {
        super();
    }
}
