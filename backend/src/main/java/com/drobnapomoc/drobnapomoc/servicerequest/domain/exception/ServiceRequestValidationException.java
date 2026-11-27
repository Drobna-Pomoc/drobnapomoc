package com.drobnapomoc.drobnapomoc.servicerequest.domain.exception;

import java.util.List;

public class ServiceRequestValidationException extends RuntimeException {

    private final List<FieldViolation> violations;

    public ServiceRequestValidationException(final List<FieldViolation> violations) {
        super("Service request failed validation: " + violations);
        this.violations = List.copyOf(violations);
    }

    public List<FieldViolation> violations() {
        return violations;
    }
}
