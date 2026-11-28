package com.drobnapomoc.drobnapomoc.auth.domain.exception;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Wymagane zalogowanie.");
    }
}

