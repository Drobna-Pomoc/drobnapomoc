package com.drobnapomoc.drobnapomoc.auth.domain.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Wymagane zalogowanie.");
    }
}

