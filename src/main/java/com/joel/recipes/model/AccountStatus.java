package com.joel.recipes.model;

public enum AccountStatus {
    // The account is no longer active due to the passage of time
    EXPIRED,

    // The account has been locked due to some reason, such as too many failed login attempts
    LOCKED,

    // The account is active and can be used normally
    ACTIVE,

    // The account's credentials (like password) have expired and need to be updated
    CREDENTIALS_EXPIRED,

    // The account has been deleted and cannot be used
    DELETED
}
