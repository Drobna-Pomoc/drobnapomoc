package com.drobnapomoc.drobnapomoc.dictionary.domain;

import java.util.Objects;

public record District(long id, long cityId, String name) {

    public District {
        Objects.requireNonNull(name, "Name cannot be null");
    }
}
