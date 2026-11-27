package com.drobnapomoc.drobnapomoc.shared.domain.exception;

/**
 * A use case needs a logged-in user, but the request is anonymous. Mapped to {@code 401 UNAUTHENTICATED}.
 */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Authentication required.");
    }
}
