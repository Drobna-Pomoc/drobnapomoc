package com.drobnapomoc.drobnapomoc.auth.domain.exception;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Konto z tym adresem email już istnieje: " + email);
    }
}
