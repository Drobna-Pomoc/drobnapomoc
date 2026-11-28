package com.drobnapomoc.drobnapomoc.servicerequest.application.port.out;

import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;

import java.util.Optional;

public interface ServiceRequestDictionaryPort {

    /** The service category, or empty when it doesn't exist or is inactive. */
    Optional<ServiceCategory> findActiveCategory(long categoryId);

    boolean districtExists(long districtId);
}
