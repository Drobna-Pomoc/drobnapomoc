package com.drobnapomoc.drobnapomoc.auth.application.service;

import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.GetCurrentUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LoginUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.LogoutUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.in.RegisterUserUseCase;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.AccountRepository;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.PasswordEncoderPort;
import com.drobnapomoc.drobnapomoc.auth.application.port.out.SessionManagerPort;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AuthService implements RegisterUserUseCase, LoginUserUseCase, LogoutUserUseCase, GetCurrentUserUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final SessionManagerPort sessionManager;

    @Override
    @Transactional
    public CurrentUserDto register(final RegisterUserCommand command) {
        final var normalizedEmail = command.email().trim().toLowerCase();

        if (accountRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyTakenException(normalizedEmail);
        }

        final var encodedPassword = passwordEncoder.encode(command.password());

        final var newAccount = Account.createClient(
                normalizedEmail,
                encodedPassword,
                command.firstName().trim(),
                command.lastName().trim(),
                command.phone().trim()
        );

        final var accountId = accountRepository.insert(newAccount);

        final var savedAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Nie odnaleziono zapisanego konta o id " + accountId));

        sessionManager.createSession(savedAccount);

        return toCurrentUserDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserDto login(final LoginUserCommand command) {
        final var normalizedEmail = command.email().trim().toLowerCase();

        final var account = accountRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), account.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        if (account.isSuspended()) {
            throw new AccountSuspendedException();
        }

        sessionManager.createSession(account);

        return toCurrentUserDto(account);
    }

    @Override
    public void logout() {
        sessionManager.invalidateSession();
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserDto getCurrentUser() {
        final var accountId = sessionManager.getAuthenticatedAccountId();

        final var account = accountRepository.findById(accountId)
                .orElseThrow(UnauthenticatedException::new);

        if (account.isSuspended()) {
            sessionManager.invalidateSession();
            throw new AccountSuspendedException();
        }

        return toCurrentUserDto(account);
    }

    private CurrentUserDto toCurrentUserDto(final Account account) {
        if (account.id() == null) {
            throw new IllegalStateException("Konto musi posiadać identyfikator ID.");
        }

        return new CurrentUserDto(
                account.id(),
                account.email(),
                account.firstName(),
                account.lastName(),
                account.phone(),
                account.roles()
        );
    }
}
