package com.drobnapomoc.drobnapomoc.servicerequest.domain;

/**
 * Constant names must stay 1:1 in sync with the {@code RequestChannel} schema in {@code api/service-requests.yaml}
 * and the PostgreSQL {@code request_channel} enum in {@code V1__init.sql}.
 */
public enum RequestChannel {
    WEB_FORM,
    PHONE
}
