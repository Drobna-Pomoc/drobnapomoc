package com.drobnapomoc.drobnapomoc.auth.application.command;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record LoginUserCommand(
        String email,
        String password
) {

    public LoginUserCommand {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
    }
}
