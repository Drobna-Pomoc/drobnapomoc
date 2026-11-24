package com.drobnapomoc.drobnapomoc.auth.infrastructure.security;

import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserPrincipal - testy adaptera UserDetails")
class UserPrincipalTest {

    private static final String EMAIL = "anna@example.com";

    @Test
    @DisplayName("Powinno poprawnie mapować dane użytkownika i role z prefiksem ROLE_")
    void shouldExposeUserDetailsAndAuthorities() {
        // given
        final var roles = Set.of(AccountRole.CLIENT, AccountRole.ADMIN);

        // when
        final var principal = new UserPrincipal(42L, EMAIL, roles);

        // then
        assertThat(principal.id()).isEqualTo(42L);
        assertThat(principal.getUsername()).isEqualTo(EMAIL);
        assertThat(principal.getPassword()).isEmpty();
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CLIENT", "ROLE_ADMIN");
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Powinno skopiować zbiór ról defensywnie i zapewnić jego niemutowalność")
    void shouldEnsureRolesImmutability() {
        // given
        final var mutableRoles = new HashSet<AccountRole>();
        mutableRoles.add(AccountRole.CLIENT);

        // when
        final var principal = new UserPrincipal(1L, EMAIL, mutableRoles);
        mutableRoles.add(AccountRole.CONTRACTOR);
        final var principalRoles = principal.roles();

        // then
        assertThat(principalRoles).containsExactly(AccountRole.CLIENT);
        assertThatThrownBy(() -> principalRoles.add(AccountRole.ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Powinno odrzucić null dla email i roles")
    void shouldRejectNullArguments() {
        // given
        final var roles = Set.of(AccountRole.CLIENT);

        // when & then
        assertThatThrownBy(() -> new UserPrincipal(1L, null, roles))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Email cannot be null");

        assertThatThrownBy(() -> new UserPrincipal(1L, EMAIL, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Roles cannot be null");
    }
}
