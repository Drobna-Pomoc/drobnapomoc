package com.drobnapomoc.drobnapomoc.auth.application.service;

import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.PasswordEncoderPort;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.SessionManagerPort;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.shared.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class AuthServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-20T12:00:00Z");
    private static final String ANNA_EMAIL = "anna.kowalska@example.com";
    private static final String SUSPENDED_EMAIL = "zawieszony@example.com";
    private static final String VALID_PHRASE = "Haslo123!";
    private static final String ENCODED_HASH = "hashed-value-123";
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

    private static Account activeAccount() {
        return new Account(
                1L,
                ANNA_EMAIL,
                ENCODED_HASH,
                FIRST_NAME,
                LAST_NAME,
                PHONE,
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                CREATED_AT,
                null
        );
    }

    private static Account suspendedAccount() {
        return new Account(
                2L,
                SUSPENDED_EMAIL,
                ENCODED_HASH,
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
    class RegisterTests {

        @Test
        void shouldRegisterNewUserSuccessfully(final SoftAssertions softly) {
            // given
            final var command = new RegisterUserCommand(
                    "  Anna.Kowalska@EXAMPLE.com  ",
                    VALID_PHRASE,
                    "  " + FIRST_NAME + " ",
                    " " + LAST_NAME + " ",
                    " " + PHONE + " "
            );
            final var savedAccount = activeAccount();
            final var accountCaptor = ArgumentCaptor.forClass(Account.class);

            given(accountRepository.existsByEmail(ANNA_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PHRASE)).willReturn(ENCODED_HASH);
            given(accountRepository.insert(accountCaptor.capture())).willReturn(1L);
            given(accountRepository.findById(1L)).willReturn(Optional.of(savedAccount));

            // when
            final var result = authService.register(command);

            // then
            final var inserted = accountCaptor.getValue();
            softly.assertThat(inserted.id()).isNull();
            softly.assertThat(inserted.email()).isEqualTo(ANNA_EMAIL);
            softly.assertThat(inserted.passwordHash()).isEqualTo(ENCODED_HASH);
            softly.assertThat(inserted.firstName()).isEqualTo(FIRST_NAME);
            softly.assertThat(inserted.lastName()).isEqualTo(LAST_NAME);
            softly.assertThat(inserted.phone()).isEqualTo(PHONE);
            softly.assertThat(inserted.roles()).containsExactly(AccountRole.CLIENT);

            softly.assertThat(result.id()).isEqualTo(1L);
            softly.assertThat(result.email()).isEqualTo(ANNA_EMAIL);
            softly.assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            softly.assertThat(result.lastName()).isEqualTo(LAST_NAME);
            softly.assertThat(result.phone()).isEqualTo(PHONE);
            softly.assertThat(result.roles()).containsExactly(AccountRole.CLIENT);

            then(sessionManager).should().createSession(savedAccount);
        }

        @Test
        void shouldThrowEmailTakenExceptionWhenEmailAlreadyExists() {
            // given
            final var command = new RegisterUserCommand(
                    "  ANNA.Kowalska@example.com ",
                    VALID_PHRASE,
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
        void shouldThrowIllegalStateWhenInsertedAccountCannotBeReloaded() {
            // given
            final var command = new RegisterUserCommand(
                    ANNA_EMAIL,
                    VALID_PHRASE,
                    FIRST_NAME,
                    LAST_NAME,
                    PHONE
            );
            given(accountRepository.existsByEmail(ANNA_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PHRASE)).willReturn(ENCODED_HASH);
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
    class LoginTests {

        @Test
        void shouldLoginSuccessfullyWithValidCredentials(final SoftAssertions softly) {
            // given
            final var command = new LoginUserCommand("  ANNA.Kowalska@example.com  ", VALID_PHRASE);
            final var account = activeAccount();

            given(accountRepository.findByEmail(ANNA_EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches(VALID_PHRASE, ENCODED_HASH)).willReturn(true);

            // when
            final var result = authService.login(command);

            // then
            softly.assertThat(result.id()).isEqualTo(1L);
            softly.assertThat(result.email()).isEqualTo(ANNA_EMAIL);
            softly.assertThat(result.roles()).containsExactly(AccountRole.CLIENT);
            then(sessionManager).should().createSession(account);
        }

        @Test
        void shouldThrowInvalidCredentialsExceptionWhenAccountDoesNotExist() {
            // given
            final var command = new LoginUserCommand("nieistnieje@example.com", VALID_PHRASE);
            given(accountRepository.findByEmail("nieistnieje@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(passwordEncoder).shouldHaveNoInteractions();
            then(sessionManager).shouldHaveNoInteractions();
        }

        @Test
        void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
            // given
            final var command = new LoginUserCommand(ANNA_EMAIL, "ZleHaslo");
            final var account = activeAccount();

            given(accountRepository.findByEmail(ANNA_EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches("ZleHaslo", ENCODED_HASH)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(sessionManager).shouldHaveNoInteractions();
        }

        @Test
        void shouldThrowAccountSuspendedExceptionWhenAccountIsSuspended() {
            // given
            final var command = new LoginUserCommand(SUSPENDED_EMAIL, VALID_PHRASE);
            final var account = suspendedAccount();

            given(accountRepository.findByEmail(SUSPENDED_EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches(VALID_PHRASE, ENCODED_HASH)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(AccountSuspendedException.class);

            then(sessionManager).shouldHaveNoInteractions();
        }
    }

    @Nested
    class SessionTests {

        @Test
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
        void shouldGetCurrentUserSuccessfully(final SoftAssertions softly) {
            // given
            final var account = activeAccount();
            given(sessionManager.getAuthenticatedAccountId()).willReturn(1L);
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // when
            final var result = authService.getCurrentUser();

            // then
            softly.assertThat(result.id()).isEqualTo(1L);
            softly.assertThat(result.email()).isEqualTo(ANNA_EMAIL);
            softly.assertThat(result.firstName()).isEqualTo(FIRST_NAME);
            softly.assertThat(result.lastName()).isEqualTo(LAST_NAME);
            softly.assertThat(result.phone()).isEqualTo(PHONE);
            softly.assertThat(result.roles()).containsExactly(AccountRole.CLIENT);
        }

        @Test
        void shouldThrowUnauthenticatedExceptionWhenNoAuthenticatedUser() {
            // given
            given(sessionManager.getAuthenticatedAccountId()).willThrow(new UnauthenticatedException());

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser())
                    .isInstanceOf(UnauthenticatedException.class);

            then(accountRepository).shouldHaveNoInteractions();
        }

        @Test
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
        void shouldInvalidateSessionAndThrowWhenCurrentAccountIsSuspended() {
            // given
            final var account = suspendedAccount();
            given(sessionManager.getAuthenticatedAccountId()).willReturn(2L);
            given(accountRepository.findById(2L)).willReturn(Optional.of(account));

            // when & then
            assertThatThrownBy(() -> authService.getCurrentUser())
                    .isInstanceOf(AccountSuspendedException.class);

            then(sessionManager).should().invalidateSession();
        }

        @Test
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
