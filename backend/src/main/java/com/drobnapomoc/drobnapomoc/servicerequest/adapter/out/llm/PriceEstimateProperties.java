package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * @param timeout how long request submission waits for the model before saving the request without an estimate
 */
@ConfigurationProperties("drobnapomoc.price-estimate")
public record PriceEstimateProperties(Duration timeout) {

    public PriceEstimateProperties {
        Objects.requireNonNull(timeout, "Timeout cannot be null");
    }
}
