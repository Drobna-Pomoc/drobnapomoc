package com.drobnapomoc.drobnapomoc.servicerequest.application.port.out;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.PriceEstimate;

import java.util.Optional;

public interface PriceEstimatorPort {

    /**
     * Suggests a price range for a request. Never throws: empty when no valid estimate is available in time.
     */
    Optional<PriceEstimate> estimate(String categoryName, String description);
}
