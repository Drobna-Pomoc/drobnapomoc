package com.drobnapomoc.drobnapomoc.auth.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import com.drobnapomoc.drobnapomoc.shared.security.SecurityConfig;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
@WebMvcTest(
        controllers = AuthExceptionHandlerTest.TestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import({AuthExceptionHandler.class, AuthExceptionHandlerTest.TestController.class})
class AuthExceptionHandlerTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void shouldHandleEmailAlreadyTaken() {
        // when
        final var result = mockMvc.get().uri("/test/auth/email-taken").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.CONFLICT,
                "EMAIL_TAKEN",
                "Konto z tym adresem email już istnieje."
        );
    }

    @Test
    void shouldHandleInvalidCredentials() {
        // when
        final var result = mockMvc.get().uri("/test/auth/invalid-credentials").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Niepoprawny email lub hasło."
        );
    }

    @Test
    void shouldHandleAccountSuspended() {
        // when
        final var result = mockMvc.get().uri("/test/auth/account-suspended").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.FORBIDDEN,
                "ACCOUNT_SUSPENDED",
                "Konto zostało zawieszone."
        );
    }

    @Test
    void shouldHandleUnauthenticatedException() {
        // when
        final var result = mockMvc.get().uri("/test/auth/unauthenticated").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                "Wymagane zalogowanie."
        );
    }

    private void assertErrorResponse(
            final MvcTestResult result,
            final HttpStatus expectedStatus,
            final String expectedCode,
            final String expectedMessage
    ) {
        assertThat(result)
                .hasStatus(expectedStatus)
                .bodyJson()
                .convertTo(ErrorResponseRest.class)
                .satisfies(error -> {
                    softly.assertThat(error.getCode()).isEqualTo(expectedCode);
                    softly.assertThat(error.getMessage()).isEqualTo(expectedMessage);
                });
    }

    @RestController
    static class TestController {

        @GetMapping("/test/auth/email-taken")
        void emailTaken() {
            throw new EmailAlreadyTakenException("anna@example.com");
        }

        @GetMapping("/test/auth/invalid-credentials")
        void invalidCredentials() {
            throw new InvalidCredentialsException();
        }

        @GetMapping("/test/auth/account-suspended")
        void accountSuspended() {
            throw new AccountSuspendedException();
        }

        @GetMapping("/test/auth/unauthenticated")
        void unauthenticated() {
            throw new UnauthenticatedException();
        }
    }
}
