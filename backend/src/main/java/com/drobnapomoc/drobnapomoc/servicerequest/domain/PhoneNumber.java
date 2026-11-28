package com.drobnapomoc.drobnapomoc.servicerequest.domain;

/**
 * Polish phone numbers, stored with the {@code +48} prefix.
 */
public final class PhoneNumber {

    private static final String POLAND_PREFIX = "+48";

    private PhoneNumber() {
    }

    /**
     * Adds the {@code +48} prefix to a number that passed the API pattern {@code ^(\+48)?[0-9]{9}$}.
     */
    public static String normalize(final String raw) {
        final var phone = raw.trim();
        return phone.startsWith(POLAND_PREFIX) ? phone : POLAND_PREFIX + phone;
    }
}
