package com.drobnapomoc.drobnapomoc.shared.domain;

import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;

import java.util.Optional;


public interface CurrentUserProvider {

    /**
     * The logged-in user, or empty for an anonymous request.
     */
    Optional<CurrentUser> current();

    /**
     * The logged-in user.
     *
     * @throws UnauthenticatedException when the request is anonymous (mapped to {@code 401})
     */
    CurrentUser require();
}
