package com.drobnapomoc.drobnapomoc.servicerequest.domain;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record ServiceRequestClient(
        String firstName,
        String lastName,
        String phone,
        String street,
        String houseNumber,
        @Nullable String apartmentNumber,
        String postalCode,
        long districtId
) {

    public ServiceRequestClient {
        Objects.requireNonNull(firstName, "FirstName cannot be null");
        Objects.requireNonNull(lastName, "LastName cannot be null");
        Objects.requireNonNull(phone, "Phone cannot be null");
        Objects.requireNonNull(street, "Street cannot be null");
        Objects.requireNonNull(houseNumber, "HouseNumber cannot be null");
        Objects.requireNonNull(postalCode, "PostalCode cannot be null");
    }
}
