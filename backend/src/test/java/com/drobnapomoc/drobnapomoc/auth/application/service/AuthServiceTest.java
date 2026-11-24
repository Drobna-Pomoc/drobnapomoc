package com.drobnapomoc.drobnapomoc.auth.application.service;

import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.SessionManagerPort;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import com.drobnapomoc.drobnapomoc.auth.domain.PasswordEncoderPort;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - testy logiki aplikacyjnej")
class AuthServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-20T12:00:00Z");
    private static final String ANNA_EMAIL = "anna.kowalska@example.com";
    private static final String SUSPENDED_EMAIL = "zawieszony@example.com";
    private static final String RAW_PASSWORD = "Haslo123!";
    private static final String HASHED_PASSWORD = "hashed-password-123";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private SessionManagerPort sessionManager;

    @InjectMocks
    private AuthService authService;

    private static Account activeAccount(final Long id) {
        return new Account(
                id,
                ANNA_EMAIL,
                HASHED_PASSWORD,
                FIRST_NAME,
                LAST_NAME,
                PHONE,
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                CREATED_AT,
                null
        );
    }

    private static Account suspendedAccount(final Long id) {
        return new Account(
                id,
                SUSPENDED_EMAIL,
                HASHED_PASSWORD,
                "Jan",
                "Zawieszony",
                PHONE,
                AccountStatus.SUSPENDED,
                Set.of(AccountRole.CLIENT),
                CREATED_AT,
                null
        );
    }

    @Nested
    @DisplayName("Rejestracja użytkownika")
    class RegisterTests {

        @Test
        @DisplayName("Powinno znormalizować dane, zapisać klienta z rolą CLIENT i utworzyć sesję")
        void shouldRegisterNewUserSuccessfully() {
            // given
            final var command = new RegisterUserCommand(
                    "  Anna.Kowalska@EXAMPLE.com  ",
                    RAW_PASSWORD,
                    "  " + FIRST_NAME + " ",
                    " " + LAST_NAME + " ",
                    " " + PHONE + " "
            );
            final var savedAccount = activeAccount(1L);
            final var accountCaptor = ArgumentCaptor.forClass(Account.class);

            given(accountRepository.existsByEmail(ANNA_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(HASHED_PASSWORD);
            given(accountRepository.insert(accountCaptor.capture())).willReturn(1L);
            given(accountRepository.findById(1L)).willReturn(Optional.of(savedAccount));

            // when
            final var result = authService.register(command);

            // then
            final var inserted = accountCaptor.getValue();
            assertThat(inserted.id()).isNull();
            assertThat(inserted.email()).isEqualTo(ANNA_EMAIL);
            assertThat(inserted.passwordHash()).isEqualTo(HASHED_PASSWORD);
            assertThat(inserted.firstName()).isEqualTo(FIRST_NAME);
            assertThat(inserted.lastName()).isEqualTo(LAST_NAME);
            assertThat(inserted.phone()).isEqualTo(PHONE);
            assertThat(inserted.roles()).containsExactly(AccountRole.CLIENT);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo(ANNA_EMAIL);
            assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            assertThat(result.lastName()).isEqualTo(LAST_NAME);
            assertThat(result.phone()).isEqualTo(PHONE);
            assertThat(result.roles()).containsExactly(AccountRole.CLIENT);

            then(sessionManager).should().createSession(savedAccount);
        }

        @Test
        @DisplayName("Powinno rzucić EmailAlreadyTakenException gdy znormalizowany email już istnieje")
        void shouldThrowEmailTakenExceptionWhenEmailAlreadyExists() {
            // given
            final var command = new RegisterUserCommand(
                    "  ANNA.Kowalska@example.com ",
                    RAW_PASSWORD,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE
            );
            given(accountRepository.existsByEmail(ANNA_EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.register(command))
                    .isInstanceOf(EmailAlreadyTakenException.class);

            then(passwordEncoder).shouldHaveNoInteractions();
            then(accountRepository).should(never()).insert(any());
            then(sessionManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Powinno rzucić IllegalStateException gdy po wstawieniu nie można odnaleźć konta")
        void shouldThrowIllegalStateWhenInsertedAccountCannotBeReloaded() {
            // given
            final var command = new RegisterUserCommand(
                    ANNA_EMAIL,
                    RAW_PASSWORD,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE
            );
            given(accountRepository.existsByEmail(ANNA_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(HASHED_PASSWORD);
            given(accountRepository.insert(any(Account.class))).willReturn(99L);
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.register(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("99");

            then(sessionManager).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Logowanie użytkownika")
    class LoginTests {

        @Test
        @DisplayName("Powinno znormalizować email, zweryfikować hasło i utworzyć sesję")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // given
            final var command = new LoginUserCommand("  ANNA.Kowalska@example.com  ", RAW_PASSWORD);
            final var account = activeAccount(1L);

            given(accountRepository.findByEmail(ANNA_EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).willReturn(true);

            // when
            final var result = authService.login(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo(ANNA_EMAIL);
            assertThat(result.roles()).containsExactly(AccountRole.CLIENT);
            then(sessionManager).should().createSession(account);
        }

        @Test
        @DisplayName("Powinno rzucić InvalidCredentialsException gdy konto nie istnieje")
        void shouldThrowInvalidCredentialsExceptionWhenAccountDoesNotExist() {
            // given
            final var command = new LoginUserCommand("nieistnieje@example.com", RAW_PASSWORD);
            given(accountRepository.findByEmail("nieistnieje@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(passwordEncoder).shouldHaveNoInteractions();
            then(sessionManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Powinno rzucić InvalidCredentialsException przy błędnym haśle")
        void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
            // given
            final var command = new LoginUserCommand(ANNA_EMAIL, "ZleHaslo");
            final var account = activeAccount(1L);

            given(accountRepository.findByEmail(ANNA_EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches("ZleHaslo", HASHED_PASSWORD)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(sessionManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Powinno rzucić AccountSuspendedException gdy konto jest zawieszone")
        void shouldThrowAccountSuspendedExceptionWhenAccountIsSuspended() {
            // given
            final var command = new LoginUserCommand(SUSPENDED_EMAIL, RAW_PASSWORD);
            final var account = suspendedAccount(2L);

            given(accountRepository.findByEmail(SUSPENDED_EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(AccountSuspendedException.class);

            then(sessionManager).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Wylogowanie i pobranie bieżącego użytkownika")
    class SessionTests {

        @Test
        @DisplayName("Powinno wylogować użytkownika przez unieważnienie sesji")
        void shouldInvalidateSessionOnLogout() {
            // given
            willDoNothing().given(sessionManager).invalidateSession();

            // when
            authService.logout();

            // then
            then(sessionManager).should().invalidateSession();
            then(sessionManager).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Powinno zwrócić dane zalogowanego aktywnego użytkownika")
        void shouldGetCurrentUserSuccessfully() {
            // given
            final var account = activeAccount(1L);
            given(sessionManager.getAuthenticatedAccountId()).willReturn(1L);
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // when
            final var result = authService.getCurrentUser();

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo(ANNA_EMAIL);
            assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            assertThat(result.lastName()).isEqualTo(LAST_NAME);
            assertThat(result.phone()).isEqualTo(PHONE);
            assertThat(result.roles()).containsExactly(AccountRole.CLIENT);
        }

        @Test
        @DisplayName("Powinno rzucić UnauthenticatedException gdy sesja nie jest uwierzytelniona")
        void shouldThrowUnauthenticatedExceptionWhenNoAuthenticatedUser() {
            // given
            given(sessionManager.getAuthenticatedAccountId()).willThrow(new UnauthenticatedException());

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser())
                    .isInstanceOf(UnauthenticatedException.class);

            then(accountRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Powinno rzucić UnauthenticatedException gdy konto z sesji już nie istnieje")
        void shouldThrowUnauthenticatedExceptionWhenAccountNotFoundInRepository() {
            // given
            given(sessionManager.getAuthenticatedAccountId()).willReturn(404L);
            given(accountRepository.findById(404L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser())
                    .isInstanceOf(UnauthenticatedException.class);

            then(sessionManager).should(never()).invalidateSession();
        }

        @Test
        @DisplayName("Powinno unieważnić sesję i rzucić AccountSuspendedException gdy konto zostało zawieszone")
        void shouldInvalidateSessionAndThrowWhenCurrentAccountIsSuspended() {
            // given
            final var account = suspendedAccount(2L);
            given(sessionManager.getAuthenticatedAccountId()).willReturn(2L);
            given(accountRepository.findById(2L)).willReturn(Optional.of(account));

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser())
                    .isInstanceOf(AccountSuspendedException.class);

            then(sessionManager).should().invalidateSession();
        }

        @Test
        @DisplayName("Powinno rzucić IllegalStateException gdy konto z repozytorium nie ma ustawionego ID")
        void shouldThrowIllegalStateWhenAccountHasNullId() {
            // given
            final var accountWithoutId = Account.createClient(
                    ANNA_EMAIL,
                    "hashed",
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE
            );
            given(sessionManager.getAuthenticatedAccountId()).willReturn(1L);
            given(accountRepository.findById(1L)).willReturn(Optional.of(accountWithoutId));

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser())
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
