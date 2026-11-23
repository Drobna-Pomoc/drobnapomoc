package com.drobnapomoc.drobnapomoc.auth.application;

import com.drobnapomoc.drobnapomoc.auth.application.command.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.SessionManagerPort;
import com.drobnapomoc.drobnapomoc.auth.application.service.AuthService;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountStatus;
import com.drobnapomoc.drobnapomoc.auth.domain.PasswordEncoderPort;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private SessionManagerPort sessionManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(accountRepository, passwordEncoder, sessionManager);
    }

    @Test
    @DisplayName("Powinna zarejestrować nowego użytkownika z rolą CLIENT i utworzyć sesję")
    void shouldRegisterNewUserSuccessfully() {
        // given
        final var command = new RegisterUserCommand(
                "anna.kowalska@example.com",
                "Haslo123!",
                "Anna",
                "Kowalska",
                "+48123456789"
        );

        when(accountRepository.existsByEmail("anna.kowalska@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Haslo123!")).thenReturn("hashed-password-123");
        when(accountRepository.insert(any(Account.class))).thenReturn(1L);

        final var savedAccount = new Account(
                1L,
                "anna.kowalska@example.com",
                "hashed-password-123",
                "Anna",
                "Kowalska",
                "+48123456789",
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                Instant.now(),
                null
        );
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));

        // when
        final var result = authService.register(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("anna.kowalska@example.com");
        assertThat(result.firstName()).isEqualTo("Anna");
        assertThat(result.lastName()).isEqualTo("Kowalska");
        assertThat(result.phone()).isEqualTo("+48123456789");
        assertThat(result.roles()).containsExactly(AccountRole.CLIENT);

        verify(sessionManager).createSession(savedAccount);
    }

    @Test
    @DisplayName("Powinna rzucić EmailAlreadyTakenException gdy email jest już zajęty")
    void shouldThrowEmailTakenExceptionWhenEmailAlreadyExists() {
        // given
        final var command = new RegisterUserCommand(
                "anna.kowalska@example.com",
                "Haslo123!",
                "Anna",
                "Kowalska",
                "+48123456789"
        );

        when(accountRepository.existsByEmail("anna.kowalska@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(command))
                .isInstanceOf(EmailAlreadyTakenException.class);
    }

    @Test
    @DisplayName("Powinna zalogować użytkownika przy poprawnych danych i utworzyć sesję")
    void shouldLoginSuccessfullyWithValidCredentials() {
        // given
        final var command = new LoginUserCommand("anna.kowalska@example.com", "Haslo123!");
        final var account = new Account(
                1L,
                "anna.kowalska@example.com",
                "hashed-password-123",
                "Anna",
                "Kowalska",
                "+48123456789",
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                Instant.now(),
                null
        );

        when(accountRepository.findByEmail("anna.kowalska@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Haslo123!", "hashed-password-123")).thenReturn(true);

        // when
        final var result = authService.login(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("anna.kowalska@example.com");
        verify(sessionManager).createSession(account);
    }

    @Test
    @DisplayName("Powinna rzucić InvalidCredentialsException przy błędnym haśle")
    void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
        // given
        final var command = new LoginUserCommand("anna.kowalska@example.com", "ZleHaslo");
        final var account = new Account(
                1L,
                "anna.kowalska@example.com",
                "hashed-password-123",
                "Anna",
                "Kowalska",
                "+48123456789",
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                Instant.now(),
                null
        );

        when(accountRepository.findByEmail("anna.kowalska@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("ZleHaslo", "hashed-password-123")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Powinna rzucić InvalidCredentialsException gdy konto nie istnieje")
    void shouldThrowInvalidCredentialsExceptionWhenAccountDoesNotExist() {
        // given
        final var command = new LoginUserCommand("nieistnieje@example.com", "Haslo123!");
        when(accountRepository.findByEmail("nieistnieje@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Powinna rzucić AccountSuspendedException gdy konto jest zawieszone")
    void shouldThrowAccountSuspendedExceptionWhenAccountIsSuspended() {
        // given
        final var command = new LoginUserCommand("zawieszony@example.com", "Haslo123!");
        final var account = new Account(
                2L,
                "zawieszony@example.com",
                "hashed-password-123",
                "Jan",
                "Zawieszony",
                "+48123456789",
                AccountStatus.SUSPENDED,
                Set.of(AccountRole.CLIENT),
                Instant.now(),
                null
        );

        when(accountRepository.findByEmail("zawieszony@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Haslo123!", "hashed-password-123")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(AccountSuspendedException.class);
    }

    @Test
    @DisplayName("Powinna poprawnie wylogować i unieważnić sesję")
    void shouldInvalidateSessionOnLogout() {
        // when
        authService.logout();

        // then
        verify(sessionManager).invalidateSession();
    }

    @Test
    @DisplayName("Powinna zwrócić zalogowanego użytkownika")
    void shouldGetCurrentUserSuccessfully() {
        // given
        final var account = new Account(
                1L,
                "anna.kowalska@example.com",
                "hashed-password-123",
                "Anna",
                "Kowalska",
                "+48123456789",
                AccountStatus.ACTIVE,
                Set.of(AccountRole.CLIENT),
                Instant.now(),
                null
        );

        when(sessionManager.getAuthenticatedAccountId()).thenReturn(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // when
        final var result = authService.getCurrentUser();

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("anna.kowalska@example.com");
    }

    @Test
    @DisplayName("Powinna rzucić UnauthenticatedException gdy brak uwierzytelnienia")
    void shouldThrowUnauthenticatedExceptionWhenNoAuthenticatedUser() {
        // given
        when(sessionManager.getAuthenticatedAccountId()).thenThrow(new UnauthenticatedException());

        // when & then
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(UnauthenticatedException.class);
    }
}
