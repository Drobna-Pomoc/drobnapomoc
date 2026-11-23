package com.drobnapomoc.drobnapomoc.auth.domain.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(final String email) {
        super("Konto z tym adresem email już istnieje: " + email);
    }
}

