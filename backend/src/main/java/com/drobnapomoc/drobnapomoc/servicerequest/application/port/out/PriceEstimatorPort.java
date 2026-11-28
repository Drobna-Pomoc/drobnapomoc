package com.drobnapomoc.drobnapomoc.servicerequest.application.port.out;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.PriceEstimate;

import java.util.Optional;

public interface PriceEstimatorPort {

    Optional<PriceEstimate> estimate(String categoryName, String description);
}
