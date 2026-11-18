package com.drobnapomoc.drobnapomoc.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Niepoprawny email lub hasło.");
    }
}
