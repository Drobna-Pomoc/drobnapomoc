package com.drobnapomoc.drobnapomoc.dictionary.domain;

import java.util.List;
import java.util.Objects;

public record City(long id, String name, List<District> districts) {

    public City {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(districts, "Districts cannot be null");
        districts = List.copyOf(districts);
    }

    public City withDistricts(final List<District> districts) {
        return new City(id, name, districts);
    }
}
