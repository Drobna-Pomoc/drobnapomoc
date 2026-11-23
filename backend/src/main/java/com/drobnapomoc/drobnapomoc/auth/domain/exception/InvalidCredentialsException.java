package com.drobnapomoc.drobnapomoc.auth.domain.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Niepoprawny email lub hasło.");
    }
}

