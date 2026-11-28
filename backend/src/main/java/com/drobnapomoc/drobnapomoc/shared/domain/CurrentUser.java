package com.drobnapomoc.drobnapomoc.shared.domain;

import java.util.Objects;
import java.util.Set;

public record CurrentUser(long id, String email, Set<AccountRole> roles) {

    public CurrentUser {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(roles, "Roles cannot be null");
        roles = Set.copyOf(roles);
    }

    public boolean hasRole(final AccountRole role) {
        return roles.contains(role);
    }
}
