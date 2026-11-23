package com.drobnapomoc.drobnapomoc.auth.domain.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class AccountSuspendedException extends RuntimeException {

    public AccountSuspendedException() {
        super("Konto zostało zawieszone.");
    }
}

