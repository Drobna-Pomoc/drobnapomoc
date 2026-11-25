package com.drobnapomoc.drobnapomoc.auth.domain.exception;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(final String email) {
        super("Account with this email already exists: " + email);
    }
}

