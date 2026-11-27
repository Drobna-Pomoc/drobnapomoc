package com.drobnapomoc.drobnapomoc.dictionary.application.port.in;

public interface DictionaryLookupUseCase {

    /** True when the service category exists and is active. */
    boolean isCategoryActive(long categoryId);

    boolean districtExists(long districtId);
}
