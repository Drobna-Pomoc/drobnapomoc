package com.drobnapomoc.drobnapomoc.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsrfCookieFilter - wymuszanie generowania tokenu CSRF")
class CsrfCookieFilterTest {

    private final CsrfCookieFilter filter = new CsrfCookieFilter();

    @Mock
    private FilterChain filterChain;

    @Mock
    private CsrfToken csrfToken;

    @Test
    @DisplayName("Powinno wywołać getToken() gdy atrybut CsrfToken znajduje się w żądaniu")
    void shouldLoadCsrfTokenWhenAttributeIsPresent() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        request.setAttribute(CsrfToken.class.getName(), csrfToken);
        given(csrfToken.getToken()).willReturn("token-value");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        then(csrfToken).should().getToken();
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    @DisplayName("Powinno przejść dalej w łańcuchu filtrów gdy brak atrybutu CsrfToken")
    void shouldContinueFilterChainWhenCsrfTokenAttributeIsMissing() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    @DisplayName("Powinno obsłużyć standardowy DefaultCsrfToken bez wyjątków")
    void shouldHandleDefaultCsrfToken() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        request.setAttribute(CsrfToken.class.getName(), new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "abc123"));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
    }
}
