package com.drobnapomoc.drobnapomoc.auth.web;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.FieldErrorRest;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@DisplayName("GlobalExceptionHandler - mapowanie wyjątków na odpowiedzi HTTP")
class GlobalExceptionHandlerTest {

    private static final String REGISTER_INPUT_OBJECT = "registerInput";
    private static final String MALFORMED_REQUEST_CODE = "MALFORMED_REQUEST";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static ErrorResponseRest assertErrorResponse(
            final ResponseEntity<ErrorResponseRest> response,
            final HttpStatus expectedStatus,
            final String expectedCode,
            final String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        final var body = response.getBody();
        assertThat(body).isNotNull();
        final var nonNullBody = Objects.requireNonNull(body);
        assertThat(nonNullBody.getCode()).isEqualTo(expectedCode);
        assertThat(nonNullBody.getMessage()).isEqualTo(expectedMessage);
        return nonNullBody;
    }

    @Test
    @DisplayName("Powinno zwrócić 409 EMAIL_TAKEN dla EmailAlreadyTakenException")
    void shouldHandleEmailAlreadyTaken() {
        // given & when
        final var response = handler.handleEmailAlreadyTaken();

        // then
        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                "EMAIL_TAKEN",
                "Konto z tym adresem email już istnieje."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 401 INVALID_CREDENTIALS dla InvalidCredentialsException")
    void shouldHandleInvalidCredentials() {
        // given & when
        final var response = handler.handleInvalidCredentials();

        // then
        assertErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Niepoprawny email lub hasło."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 403 ACCOUNT_SUSPENDED dla AccountSuspendedException")
    void shouldHandleAccountSuspended() {
        // given & when
        final var response = handler.handleAccountSuspended();

        // then
        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "ACCOUNT_SUSPENDED",
                "Konto zostało zawieszone."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 401 UNAUTHENTICATED i obsługiwać oba wyjątki braku uwierzytelnienia")
    void shouldHandleUnauthenticated() throws NoSuchMethodException {
        // given
        final var method = GlobalExceptionHandler.class.getDeclaredMethod("handleUnauthenticated");
        final var annotation = method.getAnnotation(ExceptionHandler.class);

        // when
        final var response = handler.handleUnauthenticated();

        // then
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactlyInAnyOrder(
                UnauthenticatedException.class,
                AuthenticationException.class
        );
        assertErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                "Wymagane zalogowanie."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 403 CSRF_INVALID dla CsrfException")
    void shouldHandleCsrfException() {
        // given & when
        final var response = handler.handleCsrf();

        // then
        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "CSRF_INVALID",
                "Brakujący lub niepoprawny token CSRF."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 403 ACCESS_DENIED dla AccessDeniedException")
    void shouldHandleAccessDeniedException() {
        // given & when
        final var response = handler.handleAccessDenied();

        // then
        assertErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Brak uprawnień do tej operacji."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 422 VALIDATION_FAILED i zmapować błędy pól wraz z komunikatem domyślnym")
    void shouldHandleMethodArgumentNotValid() throws NoSuchMethodException {
        // given
        final var bindingResult = new BeanPropertyBindingResult(new Object(), REGISTER_INPUT_OBJECT);
        bindingResult.addError(new FieldError(REGISTER_INPUT_OBJECT, "email", "Niepoprawny format email"));
        bindingResult.addError(new FieldError(REGISTER_INPUT_OBJECT, "phone", null, false, null, null, null));

        final Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyValidationTarget", String.class);
        final var parameter = new MethodParameter(method, 0);
        final var exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // when
        final var response = handler.handleMethodArgumentNotValid(exception);

        // then
        final var body = assertErrorResponse(
                response,
                HttpStatus.UNPROCESSABLE_CONTENT,
                "VALIDATION_FAILED",
                "Dane nie przeszły walidacji."
        );
        assertThat(body.getErrors())
                .extracting(FieldErrorRest::getField, FieldErrorRest::getMessage)
                .containsExactly(
                        tuple("email", "Niepoprawny format email"),
                        tuple("phone", "Niepoprawna wartość")
                );
    }

    @Test
    @DisplayName("Powinno zwrócić 400 MALFORMED_REQUEST dla nieczytelnego JSON")
    void shouldHandleHttpMessageNotReadable() {
        // given & when
        final var response = handler.handleHttpMessageNotReadable();

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST_CODE,
                "Treść żądania nie jest poprawnym JSON-em."
        );
    }

    @Test
    @DisplayName("Powinno zwrócić 400 MALFORMED_REQUEST z komunikatem wyjątku dla IllegalArgumentException")
    void shouldHandleIllegalArgumentException() {
        // given
        final var exception = new IllegalArgumentException("Email cannot be blank");

        // when
        final var response = handler.handleIllegalArgument(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST_CODE,
                "Email cannot be blank"
        );
    }

    @Test
    @DisplayName("Powinno zwrócić domyślny komunikat gdy IllegalArgumentException ma pusty message")
    void shouldHandleIllegalArgumentExceptionWithNullMessage() {
        // given
        final var exception = new IllegalArgumentException();

        // when
        final var response = handler.handleIllegalArgument(exception);

        // then
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST_CODE,
                "Niepoprawne parametry żądania."
        );
    }

    @SuppressWarnings("unused")
    private void dummyValidationTarget(final String value) {
        // helper method for MethodParameter construction
    }
}
