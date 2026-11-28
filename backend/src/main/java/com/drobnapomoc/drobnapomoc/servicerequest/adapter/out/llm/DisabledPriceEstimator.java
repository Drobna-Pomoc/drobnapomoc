package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.llm;

import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.PriceEstimatorPort;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.PriceEstimate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Used when price estimates are switched off ({@code drobnapomoc.price-estimate.enabled} is not {@code true}):
 * no model is called and requests are saved without an estimate.
 */
@Component
@ConditionalOnProperty(prefix = "drobnapomoc.price-estimate", name = "enabled", havingValue = "false", matchIfMissing = true)
class DisabledPriceEstimator implements PriceEstimatorPort {

    @Override
    public Optional<PriceEstimate> estimate(final String categoryName, final String description) {
        return Optional.empty();
    }
}
