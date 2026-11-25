package com.drobnapomoc.drobnapomoc.auth.domain.exception;

public class AccountSuspendedException extends RuntimeException {

    public AccountSuspendedException() {
        super("Account is suspended.");
    }
}

