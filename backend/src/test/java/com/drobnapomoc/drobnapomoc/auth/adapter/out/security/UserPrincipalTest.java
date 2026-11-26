package com.drobnapomoc.drobnapomoc.auth.adapter.out.security;

import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class UserPrincipalTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private static final String EMAIL = "anna@example.com";

    @Test
    void shouldExposeUserDetailsAndAuthorities() {
        // given
        final var roles = Set.of(AccountRole.CLIENT, AccountRole.ADMIN);

        // when
        final var principal = new UserPrincipal(42L, EMAIL, roles);

        // then
        softly.assertThat(principal.id()).isEqualTo(42L);
        softly.assertThat(principal.getUsername()).isEqualTo(EMAIL);
        softly.assertThat(principal.getPassword()).isEmpty();
        softly.assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CLIENT", "ROLE_ADMIN");
        softly.assertThat(principal.isAccountNonExpired()).isTrue();
        softly.assertThat(principal.isAccountNonLocked()).isTrue();
        softly.assertThat(principal.isCredentialsNonExpired()).isTrue();
        softly.assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void shouldEnsureRolesImmutability() {
        // given
        final var mutableRoles = new HashSet<AccountRole>();
        mutableRoles.add(AccountRole.CLIENT);

        // when
        final var principal = new UserPrincipal(1L, EMAIL, mutableRoles);
        mutableRoles.add(AccountRole.CONTRACTOR);
        final var principalRoles = principal.roles();

        // then
        softly.assertThat(principalRoles).containsExactly(AccountRole.CLIENT);
        softly.assertThatThrownBy(() -> principalRoles.add(AccountRole.ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void shouldRejectNullArguments() {
        // given
        final var roles = Set.of(AccountRole.CLIENT);

        // when & then
        softly.assertThatThrownBy(() -> new UserPrincipal(1L, null, roles))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Email cannot be null");

        softly.assertThatThrownBy(() -> new UserPrincipal(1L, EMAIL, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Roles cannot be null");
    }
}
