package com.drobnapomoc.drobnapomoc.servicerequest.domain;

/**
 * Constant names must stay 1:1 in sync with the {@code ServiceRequestStatus} schema in {@code api/service-requests.yaml}
 * and the PostgreSQL {@code service_request_status} enum in {@code V1__init.sql}.
 */
public enum ServiceRequestStatus {
    NEW,
    PRICED,
    ACCEPTED,
    IN_PROGRESS,
    WORK_CONFIRMED,
    COMPLETED,
    DISPUTED,
    UNDER_REVIEW,
    EXPIRED,
    CANCELLED
}
