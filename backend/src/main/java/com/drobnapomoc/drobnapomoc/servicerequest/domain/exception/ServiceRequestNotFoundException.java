package com.drobnapomoc.drobnapomoc.servicerequest.domain.exception;

public class ServiceRequestNotFoundException extends RuntimeException {

    public ServiceRequestNotFoundException(final long id) {
        super("Service request not found: " + id);
    }
}
