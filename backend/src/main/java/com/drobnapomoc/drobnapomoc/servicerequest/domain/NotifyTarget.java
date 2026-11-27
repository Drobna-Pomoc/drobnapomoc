package com.drobnapomoc.drobnapomoc.servicerequest.domain;

/**
 * Constant names must stay 1:1 in sync with the {@code NotifyTarget} schema in {@code api/service-requests.yaml}
 * and the PostgreSQL {@code notify_target} enum in {@code V1__init.sql}.
 */
public enum NotifyTarget {
    CLIENT,
    CAREGIVER
}
