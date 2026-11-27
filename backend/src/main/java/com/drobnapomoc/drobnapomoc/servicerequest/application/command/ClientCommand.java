package com.drobnapomoc.drobnapomoc.servicerequest.application.command;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record ClientCommand(
        String firstName,
        String lastName,
        String phone,
        String street,
        String houseNumber,
        @Nullable String apartmentNumber,
        String postalCode,
        long districtId
) {

    public ClientCommand {
        Objects.requireNonNull(firstName, "FirstName cannot be null");
        Objects.requireNonNull(lastName, "LastName cannot be null");
        Objects.requireNonNull(phone, "Phone cannot be null");
        Objects.requireNonNull(street, "Street cannot be null");
        Objects.requireNonNull(houseNumber, "HouseNumber cannot be null");
        Objects.requireNonNull(postalCode, "PostalCode cannot be null");
    }
}
