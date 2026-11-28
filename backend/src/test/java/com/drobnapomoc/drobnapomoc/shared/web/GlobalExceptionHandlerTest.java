package com.drobnapomoc.drobnapomoc.shared.web;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.FieldErrorRest;
import com.drobnapomoc.drobnapomoc.shared.domain.exception.UnauthenticatedException;
import com.drobnapomoc.drobnapomoc.shared.security.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@ExtendWith(SoftAssertionsExtension.class)
@WebMvcTest(
        controllers = GlobalExceptionHandlerTest.TestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    private static final String MALFORMED_REQUEST_CODE = "MALFORMED_REQUEST";
    private static final String VALIDATION_PATH = "/test/validation";
    private static final String ILLEGAL_ARGUMENT_PATH = "/test/illegal-argument";
    private static final String UNAUTHENTICATED_MESSAGE = "Wymagane zalogowanie.";

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void shouldHandleAuthenticationException(final SoftAssertions softly) {
        // when
        final var result = mockMvc.get().uri("/test/authentication-exception").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                UNAUTHENTICATED_MESSAGE,
                softly
        );
    }

    @Test
    void testHandleUnauthenticated_givenSharedUnauthenticatedException_shouldReturnUnauthorized(final SoftAssertions softly) {
        // when
        final var result = mockMvc.get().uri("/test/unauthenticated").exchange();

        // then
        assertErrorResponse(result, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", UNAUTHENTICATED_MESSAGE, softly);
    }

    @Test
    void shouldHandleCsrfException(final SoftAssertions softly) {
        // when
        final var result = mockMvc.get().uri("/test/csrf").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.FORBIDDEN,
                "CSRF_INVALID",
                "Brakujący lub niepoprawny token CSRF.",
                softly
        );
    }

    @Test
    void shouldHandleAccessDeniedException(final SoftAssertions softly) {
        // when
        final var result = mockMvc.get().uri("/test/access-denied").exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Brak uprawnień do tej operacji.",
                softly
        );
    }

    @Test
    void shouldHandleBeanValidationFailure(final SoftAssertions softly) {
        // given
        final var invalidPayload = """
                {
                  "email": "niepoprawny-email",
                  "phone": "+48123456789"
                }
                """;

        // when
        final var result = mockMvc.post()
                .uri(VALIDATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson()
                .convertTo(ErrorResponseRest.class)
                .satisfies(error -> {
                    softly.assertThat(error.getCode()).isEqualTo("VALIDATION_FAILED");
                    softly.assertThat(error.getMessage()).isEqualTo("Dane nie przeszły walidacji.");
                    softly.assertThat(error.getErrors())
                            .extracting(FieldErrorRest::getField, FieldErrorRest::getMessage)
                            .containsExactly(tuple("email", "Niepoprawny format email"));
                });
    }

    @Test
    void shouldFallbackToDefaultFieldErrorMessageWhenNull(final SoftAssertions softly) {
        // given
        final var payload = """
                {
                  "phone": "invalid"
                }
                """;

        // when
        final var result = mockMvc.post()
                .uri("/test/validation-null-message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson()
                .convertTo(ErrorResponseRest.class)
                .satisfies(error -> {
                    softly.assertThat(error.getCode()).isEqualTo("VALIDATION_FAILED");
                    softly.assertThat(error.getErrors())
                            .extracting(FieldErrorRest::getField, FieldErrorRest::getMessage)
                            .containsExactly(tuple("phone", "Niepoprawna wartość"));
                });
    }

    @Test
    void shouldHandleHttpMessageNotReadable(final SoftAssertions softly) {
        // given
        final var malformedJson = "{ \"email\": ";

        // when
        final var result = mockMvc.post()
                .uri(VALIDATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
                .exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST_CODE,
                "Treść żądania nie jest poprawnym JSON-em.",
                softly
        );
    }

    @Test
    void shouldHandleIllegalArgumentException(final SoftAssertions softly) {
        // when
        final var result = mockMvc.get()
                .uri(ILLEGAL_ARGUMENT_PATH)
                .param("withMessage", "true")
                .exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST_CODE,
                "Email cannot be blank",
                softly
        );
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWithNullMessage(final SoftAssertions softly) {
        // when
        final var result = mockMvc.get()
                .uri(ILLEGAL_ARGUMENT_PATH)
                .param("withMessage", "false")
                .exchange();

        // then
        assertErrorResponse(
                result,
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST_CODE,
                "Niepoprawne parametry żądania.",
                softly
        );
    }

    private static void assertErrorResponse(
            final MvcTestResult result,
            final HttpStatus expectedStatus,
            final String expectedCode,
            final String expectedMessage,
            final SoftAssertions softly
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
    @NullMarked
    static class TestController {

        @InitBinder("nullMessagePayload")
        void initNullMessageBinder(final WebDataBinder binder) {
            binder.addValidators(new Validator() {
                @Override
                public boolean supports(final Class<?> clazz) {
                    return NullMessagePayload.class.isAssignableFrom(clazz);
                }

                @Override
                public void validate(final Object target, final Errors errors) {
                    errors.rejectValue("phone", "phone.invalid");
                }
            });
        }

        @GetMapping("/test/authentication-exception")
        void authenticationException() {
            throw new BadCredentialsException(UNAUTHENTICATED_MESSAGE);
        }

        @GetMapping("/test/unauthenticated")
        void unauthenticated() {
            throw new UnauthenticatedException();
        }

        @GetMapping("/test/csrf")
        void csrf() {
            throw new MissingCsrfTokenException("X-XSRF-TOKEN");
        }

        @GetMapping("/test/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("Forbidden");
        }

        @PostMapping(VALIDATION_PATH)
        ValidationPayload validate(@Valid @RequestBody final ValidationPayload payload) {
            return payload;
        }

        @PostMapping("/test/validation-null-message")
        NullMessagePayload validateNullMessage(@Valid @RequestBody final NullMessagePayload payload) {
            return payload;
        }

        @GetMapping(ILLEGAL_ARGUMENT_PATH)
        void illegalArgument(@RequestParam final boolean withMessage) {
            if (withMessage) {
                throw new IllegalArgumentException("Email cannot be blank");
            }
            throw new IllegalArgumentException();
        }
    }

    record ValidationPayload(
            @NotBlank(message = "Email jest wymagany")
            @Email(message = "Niepoprawny format email")
            String email,
            String phone
    ) {}

    record NullMessagePayload(String phone) {}
}
