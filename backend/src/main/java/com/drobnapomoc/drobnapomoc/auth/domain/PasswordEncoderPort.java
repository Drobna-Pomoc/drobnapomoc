package com.drobnapomoc.drobnapomoc.auth.domain;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PasswordEncoderPort {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
