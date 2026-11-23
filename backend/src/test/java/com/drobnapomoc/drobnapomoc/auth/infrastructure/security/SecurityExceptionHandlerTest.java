package com.drobnapomoc.drobnapomoc.auth.infrastructure.security;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.auth.web.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionHandlerTest {

    @Mock
    private HandlerExceptionResolver resolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("RestAuthenticationEntryPoint delegates to HandlerExceptionResolver")
    void restAuthenticationEntryPoint_delegatesToResolver() {
        final var entryPoint = new RestAuthenticationEntryPoint(resolver);
        final AuthenticationException exception = new BadCredentialsException("Bad credentials");

        entryPoint.commence(request, response, exception);

        verify(resolver).resolveException(request, response, null, exception);
    }

    @Test
    @DisplayName("RestAccessDeniedHandler delegates to HandlerExceptionResolver")
    void restAccessDeniedHandler_delegatesToResolver() {
        final var handler = new RestAccessDeniedHandler(resolver);
        final AccessDeniedException exception = new AccessDeniedException("Access denied");

        handler.handle(request, response, exception);

        verify(resolver).resolveException(request, response, null, exception);
    }

    @Test
    @DisplayName("GlobalExceptionHandler handles AuthenticationException with 401 UNAUTHENTICATED")
    void globalExceptionHandler_handlesAuthenticationException() {
        final var exceptionHandler = new GlobalExceptionHandler();
        final AuthenticationException exception = new BadCredentialsException("Unauthenticated");

        final var responseEntity = exceptionHandler.handleAuthentication(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getCode()).isEqualTo("UNAUTHENTICATED");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Wymagane zalogowanie.");
    }

    @Test
    @DisplayName("GlobalExceptionHandler handles CsrfException with 403 CSRF_INVALID")
    void globalExceptionHandler_handlesCsrfException() {
        final var exceptionHandler = new GlobalExceptionHandler();
        final CsrfException exception = new CsrfException("CSRF invalid") {};

        final var responseEntity = exceptionHandler.handleCsrf(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getCode()).isEqualTo("CSRF_INVALID");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Brakujący lub niepoprawny token CSRF.");
    }

    @Test
    @DisplayName("GlobalExceptionHandler handles AccessDeniedException with 403 ACCESS_DENIED")
    void globalExceptionHandler_handlesAccessDeniedException() {
        final var exceptionHandler = new GlobalExceptionHandler();
        final AccessDeniedException exception = new AccessDeniedException("Forbidden");

        final var responseEntity = exceptionHandler.handleAccessDenied(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getCode()).isEqualTo("ACCESS_DENIED");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Brak uprawnień do tej operacji.");
    }
}
