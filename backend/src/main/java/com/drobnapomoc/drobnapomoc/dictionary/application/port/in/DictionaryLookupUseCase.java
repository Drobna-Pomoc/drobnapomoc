package com.drobnapomoc.drobnapomoc.dictionary.application.port.in;

import com.drobnapomoc.drobnapomoc.shared.domain.ServiceCategory;

import java.util.Optional;

public interface DictionaryLookupUseCase {

    /** The service category, or empty when it doesn't exist or is inactive. */
    Optional<ServiceCategory> findActiveCategory(long categoryId);

    boolean districtExists(long districtId);
}
