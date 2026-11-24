package com.drobnapomoc.drobnapomoc.auth.web;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.FieldErrorRest;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponseRest> handleEmailAlreadyTaken() {
        return errorResponse(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Konto z tym adresem email już istnieje.");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseRest> handleInvalidCredentials() {
        return errorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Niepoprawny email lub hasło.");
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ErrorResponseRest> handleAccountSuspended() {
        return errorResponse(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", "Konto zostało zawieszone.");
    }

    @ExceptionHandler({UnauthenticatedException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponseRest> handleUnauthenticated() {
        return errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Wymagane zalogowanie.");
    }

    @ExceptionHandler(CsrfException.class)
    public ResponseEntity<ErrorResponseRest> handleCsrf() {
        return errorResponse(HttpStatus.FORBIDDEN, "CSRF_INVALID", "Brakujący lub niepoprawny token CSRF.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseRest> handleAccessDenied() {
        return errorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Brak uprawnień do tej operacji.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseRest> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        final var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorRest(
                        fieldError.getField(),
                        Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Niepoprawna wartość")
                ))
                .toList();

        final var error = new ErrorResponseRest("VALIDATION_FAILED", "Dane nie przeszły walidacji.");
        error.setErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseRest> handleHttpMessageNotReadable() {
        return errorResponse(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Treść żądania nie jest poprawnym JSON-em.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseRest> handleIllegalArgument(final IllegalArgumentException ex) {
        final var message = Objects.requireNonNullElse(ex.getMessage(), "Niepoprawne parametry żądania.");
        return errorResponse(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", message);
    }

    private ResponseEntity<ErrorResponseRest> errorResponse(
            final HttpStatus status,
            final String code,
            final String message
    ) {
        return ResponseEntity.status(status).body(new ErrorResponseRest(code, message));
    }
}
