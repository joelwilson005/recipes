package com.joel.recipes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue
    private UUID id;
    private String firstname;
    private String lastname;
    private String password;
    private String email;
    private boolean isEmailVerified;
    private Timestamp emailVerificationTimestamp;
    private Timestamp accountCreationDate;
    private String username;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private VerificationToken emailVerificationToken;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private VerificationToken passwordResetToken;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recipe> recipes;
    @ManyToMany
    private Set<UserEntity> subscribers;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeComment> comments;
    @OneToMany
    private Set<Recipe> favourites;
    @ManyToMany
    private Set<Role> authorities;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens;
    private AccountStatus accountStatus;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountStatus != AccountStatus.EXPIRED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return accountStatus != AccountStatus.CREDENTIALS_EXPIRED;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE;
    }
}
