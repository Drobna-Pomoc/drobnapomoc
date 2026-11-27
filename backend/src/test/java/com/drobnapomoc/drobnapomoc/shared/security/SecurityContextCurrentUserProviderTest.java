package com.drobnapomoc.drobnapomoc.shared.security;

import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUser;
import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityContextCurrentUserProviderTest {

    private static final UserPrincipal ANNA =
            new UserPrincipal(77L, "anna@example.com", Set.of(AccountRole.CLIENT, AccountRole.CAREGIVER));

    private final SecurityContextCurrentUserProvider provider = new SecurityContextCurrentUserProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrent_givenNoAuthentication_shouldReturnEmpty() {
        assertThat(provider.current()).isEmpty();
    }

    @Test
    void testCurrent_givenAnonymousAuthentication_shouldReturnEmpty() {
        // given
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        // when & then
        assertThat(provider.current()).isEmpty();
    }

    @Test
    void testCurrent_givenForeignPrincipal_shouldReturnEmpty() {
        // given
        final var authentication = new TestingAuthenticationToken("someone", null, "ROLE_CLIENT");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when & then
        assertThat(provider.current()).isEmpty();
    }

    @Test
    void testCurrent_givenUserPrincipal_shouldReturnIdEmailAndRoles() {
        // given
        authenticateAs(ANNA);

        // when & then
        assertThat(provider.current()).contains(
                new CurrentUser(77L, "anna@example.com", Set.of(AccountRole.CLIENT, AccountRole.CAREGIVER)));
    }

    @Test
    void testRequire_givenUserPrincipal_shouldReturnUser() {
        // given
        authenticateAs(ANNA);

        // when & then
        assertThat(provider.require().id()).isEqualTo(77L);
    }

    @Test
    void testRequire_givenNoAuthentication_shouldThrowUnauthenticated() {
        assertThatThrownBy(provider::require).isInstanceOf(UnauthenticatedException.class);
    }

    private static void authenticateAs(final UserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
