package com.drobnapomoc.drobnapomoc.shared.domain;

import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;

import java.util.Optional;

/**
 * Gives application services access to the logged-in user of the current request
 */
public interface CurrentUserProvider {

    /**
     * The logged-in user, or empty for an anonymous request. For use cases that work with and without a session.
     */
    Optional<CurrentUser> current();

    /**
     * The logged-in user, for use cases that only make sense with a session.
     *
     * @throws UnauthenticatedException when the request is anonymous (mapped to {@code 401})
     */
    CurrentUser require();
}
