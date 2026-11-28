package com.drobnapomoc.drobnapomoc.auth.domain;

import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record Account(
        @Nullable Long id,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String phone,
        AccountStatus status,
        Set<AccountRole> roles,
        @Nullable Instant createdAt,
        @Nullable Instant anonymizedAt
) {

    public Account {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(passwordHash, "PasswordHash cannot be null");
        Objects.requireNonNull(firstName, "FirstName cannot be null");
        Objects.requireNonNull(lastName, "LastName cannot be null");
        Objects.requireNonNull(phone, "Phone cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(roles, "Roles cannot be null");

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (firstName.isBlank()) {
            throw new IllegalArgumentException("FirstName cannot be blank");
        }
        if (lastName.isBlank()) {
            throw new IllegalArgumentException("LastName cannot be blank");
        }
        if (phone.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be blank");
        }

        roles = Set.copyOf(roles);
    }

    public static Account createClient(
            final String email,
            final String passwordHash,
            final String firstName,
            final String lastName,
            final String phone
    ) {
        return new Account(
                null,
                email,
                passwordHash,
                firstName,
                lastName,
                phone,
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                null,
                null
        );
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == AccountStatus.SUSPENDED;
    }

    public boolean hasRole(final AccountRole role) {
        Objects.requireNonNull(role, "Role cannot be null");
        return roles.contains(role);
    }
}
