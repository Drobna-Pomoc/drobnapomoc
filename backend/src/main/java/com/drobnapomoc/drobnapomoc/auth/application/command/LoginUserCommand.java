package com.drobnapomoc.drobnapomoc.auth.application.command;

import java.util.Objects;

public record LoginUserCommand(
        String email,
        String password
) {

    public LoginUserCommand {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
    }
}
