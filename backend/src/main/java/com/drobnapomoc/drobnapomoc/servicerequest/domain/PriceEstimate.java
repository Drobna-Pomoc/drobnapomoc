package com.drobnapomoc.drobnapomoc.servicerequest.domain;

public record PriceEstimate(long minMinor, long maxMinor) {

    public PriceEstimate {
        if (minMinor <= 0) {
            throw new IllegalArgumentException("Estimate minimum must be positive: " + minMinor);
        }
        if (maxMinor < minMinor) {
            throw new IllegalArgumentException("Estimate maximum " + maxMinor + " is below minimum " + minMinor);
        }
    }
}
