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

    private static final String VALID_PHRASE = "Haslo123!";
    private static final String ENCODED_HASH = "$2a$10$encodedHash";

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BCryptPasswordEncoderAdapter adapter;

    @Test
    @DisplayName("Powinno zakodować hasło przez PasswordEncoder")
    void shouldEncodePassword() {
        // given
        given(passwordEncoder.encode(VALID_PHRASE)).willReturn(ENCODED_HASH);

        // when
        final var encoded = adapter.encode(VALID_PHRASE);

        // then
        assertThat(encoded).isEqualTo(ENCODED_HASH);
        then(passwordEncoder).should().encode(VALID_PHRASE);
    }

    @Test
    @DisplayName("Powinno zwrócić true gdy surowe hasło pasuje do hasha")
    void shouldReturnTrueWhenPasswordMatches() {
        // given
        given(passwordEncoder.matches(VALID_PHRASE, ENCODED_HASH)).willReturn(true);

        // when
        final var matches = adapter.matches(VALID_PHRASE, ENCODED_HASH);

        // then
        assertThat(matches).isTrue();
        then(passwordEncoder).should().matches(VALID_PHRASE, ENCODED_HASH);
    }

    @Test
    @DisplayName("Powinno zwrócić false gdy surowe hasło nie pasuje do hasha")
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        // given
        final var wrongInput = "ZleHaslo";
        given(passwordEncoder.matches(wrongInput, ENCODED_HASH)).willReturn(false);

        // when
        final var matches = adapter.matches(wrongInput, ENCODED_HASH);

        // then
        assertThat(matches).isFalse();
        then(passwordEncoder).should().matches(wrongInput, ENCODED_HASH);
    }
}
