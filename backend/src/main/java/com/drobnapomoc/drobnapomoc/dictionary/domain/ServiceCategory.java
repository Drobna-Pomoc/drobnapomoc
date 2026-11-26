package com.drobnapomoc.drobnapomoc.dictionary.domain;

import java.util.Objects;

public record ServiceCategory(long id, String name) {

    public ServiceCategory {
        Objects.requireNonNull(name, "Name cannot be null");
    }
}
