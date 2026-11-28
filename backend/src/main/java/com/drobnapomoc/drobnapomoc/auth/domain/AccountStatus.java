package com.drobnapomoc.drobnapomoc.auth.domain;

/**
 * Lifecycle status of a user account.
 *
 * <p>Constant names must stay strictly 1:1 in sync with the PostgreSQL {@code account_status} enum
 * defined in the database migration ({@code V1__init.sql}) and any corresponding OpenAPI schema
 * (currently {@link #SUSPENDED} maps to the {@code ACCOUNT_SUSPENDED} error code in {@code api/auth.yaml}).
 */
public enum AccountStatus {
    ACTIVE,
    SUSPENDED
}
