package com.drobnapomoc.drobnapomoc.shared.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserTest {

    private final CurrentUser contractor =
            new CurrentUser(7L, "fachowiec@example.com", Set.of(AccountRole.CLIENT, AccountRole.CONTRACTOR));

    @Test
    void testHasRole_givenAssignedRole_shouldReturnTrue() {
        assertThat(contractor.hasRole(AccountRole.CONTRACTOR)).isTrue();
    }

    @Test
    void testHasRole_givenRoleNotAssigned_shouldReturnFalse() {
        assertThat(contractor.hasRole(AccountRole.ADMIN)).isFalse();
    }
}
