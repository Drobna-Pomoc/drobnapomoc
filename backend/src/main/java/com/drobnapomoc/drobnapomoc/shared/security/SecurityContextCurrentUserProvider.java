package com.drobnapomoc.drobnapomoc.shared.security;

import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUser;
import com.drobnapomoc.drobnapomoc.shared.domain.CurrentUserProvider;
import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<CurrentUser> current() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(new CurrentUser(principal.id(), principal.email(), principal.roles()));
        }
        return Optional.empty();
    }

    @Override
    public CurrentUser require() {
        return current().orElseThrow(UnauthenticatedException::new);
    }
}
