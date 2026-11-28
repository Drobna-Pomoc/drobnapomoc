package com.drobnapomoc.drobnapomoc.servicerequest.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * @param expiry how long a new request stays open before it expires
 */
@ConfigurationProperties("drobnapomoc.service-request")
public record ServiceRequestProperties(Duration expiry) {

    public ServiceRequestProperties {
        Objects.requireNonNull(expiry, "Expiry cannot be null");
    }
}
