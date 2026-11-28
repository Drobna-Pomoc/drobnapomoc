package com.drobnapomoc.drobnapomoc.auth.application.dto;

import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;

import java.util.Objects;
import java.util.Set;

public record CurrentUserDto(
        long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Set<AccountRole> roles
) {

    public CurrentUserDto {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(firstName, "FirstName cannot be null");
        Objects.requireNonNull(lastName, "LastName cannot be null");
        Objects.requireNonNull(phone, "Phone cannot be null");
        Objects.requireNonNull(roles, "Roles cannot be null");
        roles = Set.copyOf(roles);
    }
}
