package com.drobnapomoc.drobnapomoc.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionHandlerTest {

    @Mock
    private HandlerExceptionResolver resolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void shouldDelegateAuthenticationExceptionToResolver() {
        // given
        final var entryPoint = new RestAuthenticationEntryPoint(resolver);
        final var exception = new BadCredentialsException("Bad credentials");

        // when
        entryPoint.commence(request, response, exception);

        // then
        then(resolver).should().resolveException(request, response, null, exception);
    }

    @Test
    void shouldDelegateAccessDeniedExceptionToResolver() {
        // given
        final var handler = new RestAccessDeniedHandler(resolver);
        final var exception = new AccessDeniedException("Access denied");

        // when
        handler.handle(request, response, exception);

        // then
        then(resolver).should().resolveException(request, response, null, exception);
    }
}
