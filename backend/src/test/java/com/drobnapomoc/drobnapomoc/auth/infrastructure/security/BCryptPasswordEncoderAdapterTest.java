package com.drobnapomoc.drobnapomoc.auth.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BCryptPasswordEncoderAdapter - delegacja do PasswordEncoder")
class BCryptPasswordEncoderAdapterTest {

    private static final String RAW_PASSWORD = "Haslo123!";
    private static final String ENCODED_HASH = "$2a$10$encodedHash";

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BCryptPasswordEncoderAdapter adapter;

    @Test
    @DisplayName("Powinno zakodować hasło przez PasswordEncoder")
    void shouldEncodePassword() {
        // given
        given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_HASH);

        // when
        final var encoded = adapter.encode(RAW_PASSWORD);

        // then
        assertThat(encoded).isEqualTo(ENCODED_HASH);
        then(passwordEncoder).should().encode(RAW_PASSWORD);
    }

    @Test
    @DisplayName("Powinno zwrócić true gdy surowe hasło pasuje do hasha")
    void shouldReturnTrueWhenPasswordMatches() {
        // given
        given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_HASH)).willReturn(true);

        // when
        final var matches = adapter.matches(RAW_PASSWORD, ENCODED_HASH);

        // then
        assertThat(matches).isTrue();
        then(passwordEncoder).should().matches(RAW_PASSWORD, ENCODED_HASH);
    }

    @Test
    @DisplayName("Powinno zwrócić false gdy surowe hasło nie pasuje do hasha")
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        // given
        final var wrongPassword = "ZleHaslo";
        given(passwordEncoder.matches(wrongPassword, ENCODED_HASH)).willReturn(false);

        // when
        final var matches = adapter.matches(wrongPassword, ENCODED_HASH);

        // then
        assertThat(matches).isFalse();
        then(passwordEncoder).should().matches(wrongPassword, ENCODED_HASH);
    }
}
