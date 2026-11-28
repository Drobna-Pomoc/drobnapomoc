package com.drobnapomoc.drobnapomoc.servicerequest.domain.exception;

import java.util.Objects;

/**
 * A business rule violation of one input field; {@code field} is the body path joined by dots.
 */
public record FieldViolation(String field, String message) {

    public FieldViolation {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");
    }
}
