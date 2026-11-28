package com.drobnapomoc.drobnapomoc.auth.adapter.out.security;

import com.drobnapomoc.drobnapomoc.auth.application.port.out.SessionManagerPort;
import com.drobnapomoc.drobnapomoc.auth.domain.Account;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import com.drobnapomoc.drobnapomoc.shared.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
class SpringSessionManagerAdapter implements SessionManagerPort {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final SecurityContextRepository securityContextRepository;

    @Override
    public void createSession(final Account account) {
        Objects.requireNonNull(account.id(), "Konto musi posiadać identyfikator ID.");

        final var principal = new UserPrincipal(account.id(), account.email(), account.roles());
        final var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true);
        securityContextRepository.saveContext(context, request, response);
    }

    @Override
    public void invalidateSession() {
        final var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public long getAuthenticatedAccountId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthenticatedException();
        }

        final var principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.id();
        }

        throw new UnauthenticatedException();
    }
}
