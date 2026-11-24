package com.drobnapomoc.drobnapomoc.auth.infrastructure.security;

import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringSessionManagerAdapter - zarządzanie sesją i kontekstem Spring Security")
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
    @DisplayName("Tworzenie sesji")
    class CreateSessionTests {

        @Test
        @DisplayName("Powinno utworzyć sesję HTTP, ustawić UserPrincipal w SecurityContext i zapisać kontekst")
        void shouldCreateSessionAndPersistSecurityContext() {
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
            assertThat(principal.id()).isEqualTo(15L);
            assertThat(principal.email()).isEqualTo(EMAIL);
            assertThat(principal.roles()).containsExactly(AccountRole.CLIENT);

            then(securityContextRepository).should().saveContext(contextCaptor.capture(), eq(request), eq(response));
            assertThat(contextCaptor.getValue().getAuthentication()).isEqualTo(authentication);
        }

        @Test
        @DisplayName("Powinno rzucić NullPointerException gdy konto nie posiada ID")
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
    @DisplayName("Unieważnianie sesji")
    class InvalidateSessionTests {

        @Test
        @DisplayName("Powinno unieważnić istniejącą sesję HTTP i wyczyścić SecurityContextHolder")
        void shouldInvalidateExistingSessionAndClearContext() {
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
            assertThat(session.isInvalid()).isTrue();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Powinno wyczyścić SecurityContextHolder bezpiecznie gdy nie ma sesji HTTP")
        void shouldClearContextWhenNoHttpSessionExists() {
            // given
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("user", "pass", "ROLE_CLIENT")
            );

            // when
            adapter.invalidateSession();

            // then
            assertThat(request.getSession(false)).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("Pobieranie ID uwierzytelnionego konta")
    class GetAuthenticatedAccountIdTests {

        @Test
        @DisplayName("Powinno zwrócić ID z UserPrincipal dla uwierzytelnionego użytkownika")
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
        @DisplayName("Powinno rzucić UnauthenticatedException gdy Authentication jest null")
        void shouldThrowWhenAuthenticationIsNull() {
            // given
            SecurityContextHolder.clearContext();

            // when & then
            assertThatThrownBy(() -> adapter.getAuthenticatedAccountId())
                    .isInstanceOf(UnauthenticatedException.class);
        }

        @Test
        @DisplayName("Powinno rzucić UnauthenticatedException gdy użytkownik nie jest uwierzytelniony")
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
        @DisplayName("Powinno rzucić UnauthenticatedException dla AnonymousAuthenticationToken")
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
        @DisplayName("Powinno rzucić UnauthenticatedException gdy principal nie jest instancją UserPrincipal")
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
