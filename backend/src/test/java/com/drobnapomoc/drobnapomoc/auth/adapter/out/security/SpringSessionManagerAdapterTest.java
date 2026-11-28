package com.drobnapomoc.drobnapomoc.auth.adapter.out.security;

import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class SpringSessionManagerAdapterTest {

    private static final String EMAIL = "anna@example.com";

    @Mock
    private SecurityContextRepository securityContextRepository;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private SpringSessionManagerAdapter adapter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        adapter = new SpringSessionManagerAdapter(request, response, securityContextRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class CreateSessionTests {

        @Test
        void shouldCreateSessionAndPersistSecurityContext(final SoftAssertions softly) {
            // given
            final var account = new Account(
                    15L,
                    EMAIL,
                    "hashed",
                    "Anna",
                    "Kowalska",
                    "+48123456789",
                    AccountStatus.ACTIVE,
                    Set.of(AccountRole.CLIENT),
                    Instant.now(),
                    null
            );
            final var contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);

            // when
            adapter.createSession(account);

            // then
            assertThat(request.getSession(false)).isNotNull();

            final var authentication = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
            assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);

            final var principal = (UserPrincipal) Objects.requireNonNull(authentication.getPrincipal());
            then(securityContextRepository).should().saveContext(contextCaptor.capture(), eq(request), eq(response));

            softly.assertThat(principal.id()).isEqualTo(15L);
            softly.assertThat(principal.email()).isEqualTo(EMAIL);
            softly.assertThat(principal.roles()).containsExactly(AccountRole.CLIENT);
            softly.assertThat(contextCaptor.getValue().getAuthentication()).isEqualTo(authentication);
        }

        @Test
        void shouldThrowWhenAccountIdIsNull() {
            // given
            final var accountWithoutId = Account.createClient(
                    EMAIL,
                    "hashed",
                    "Anna",
                    "Kowalska",
                    "+48123456789"
            );

            // when & then
            assertThatThrownBy(() -> adapter.createSession(accountWithoutId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Konto musi posiadać identyfikator ID.");

            then(securityContextRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    class InvalidateSessionTests {

        @Test
        void shouldInvalidateExistingSessionAndClearContext(final SoftAssertions softly) {
            // given
            final var session = new MockHttpSession();
            request.setSession(session);
            final var principal = new UserPrincipal(1L, EMAIL, Set.of(AccountRole.CLIENT));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            );

            // when
            adapter.invalidateSession();

            // then
            softly.assertThat(session.isInvalid()).isTrue();
            softly.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        void shouldClearContextWhenNoHttpSessionExists(final SoftAssertions softly) {
            // given
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("user", "pass", "ROLE_CLIENT")
            );

            // when
            adapter.invalidateSession();

            // then
            softly.assertThat(request.getSession(false)).isNull();
            softly.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    class GetAuthenticatedAccountIdTests {

        @Test
        void shouldReturnAccountIdForAuthenticatedUserPrincipal() {
            // given
            final var principal = new UserPrincipal(77L, EMAIL, Set.of(AccountRole.CLIENT));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            );

            // when
            final var accountId = adapter.getAuthenticatedAccountId();

            // then
            assertThat(accountId).isEqualTo(77L);
        }

        @Test
        void shouldThrowWhenAuthenticationIsNull() {
            // given
            SecurityContextHolder.clearContext();

            // when & then
            assertThatThrownBy(() -> adapter.getAuthenticatedAccountId())
                    .isInstanceOf(UnauthenticatedException.class);
        }

        @Test
        void shouldThrowWhenAuthenticationIsNotAuthenticated() {
            // given
            final var unauthenticated = new TestingAuthenticationToken("user", "pass");
            unauthenticated.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(unauthenticated);

            // when & then
            assertThatThrownBy(() -> adapter.getAuthenticatedAccountId())
                    .isInstanceOf(UnauthenticatedException.class);
        }

        @Test
        void shouldThrowForAnonymousAuthenticationToken() {
            // given
            final var anonymous = new AnonymousAuthenticationToken(
                    "key",
                    "anonymousUser",
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
            );
            SecurityContextHolder.getContext().setAuthentication(anonymous);

            // when & then
            assertThatThrownBy(() -> adapter.getAuthenticatedAccountId())
                    .isInstanceOf(UnauthenticatedException.class);
        }

        @Test
        void shouldThrowWhenPrincipalIsNotUserPrincipal() {
            // given
            final var token = new TestingAuthenticationToken("plain-string-principal", null, "ROLE_CLIENT");
            token.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(token);

            // when & then
            assertThatThrownBy(() -> adapter.getAuthenticatedAccountId())
                    .isInstanceOf(UnauthenticatedException.class);
        }
    }
}
