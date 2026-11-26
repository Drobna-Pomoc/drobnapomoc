package com.drobnapomoc.drobnapomoc.auth.application.command;

import java.util.Objects;

public record RegisterUserCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone
) {

    public RegisterUserCommand {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(firstName, "FirstName cannot be null");
        Objects.requireNonNull(lastName, "LastName cannot be null");
        Objects.requireNonNull(phone, "Phone cannot be null");
    }
}
