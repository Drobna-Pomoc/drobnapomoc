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

@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponseRest> handleEmailAlreadyTaken(final EmailAlreadyTakenException ex) {
        final var error = new ErrorResponseRest("EMAIL_TAKEN", "Konto z tym adresem email już istnieje.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseRest> handleInvalidCredentials(final InvalidCredentialsException ex) {
        final var error = new ErrorResponseRest("INVALID_CREDENTIALS", "Niepoprawny email lub hasło.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ErrorResponseRest> handleAccountSuspended(final AccountSuspendedException ex) {
        final var error = new ErrorResponseRest("ACCOUNT_SUSPENDED", "Konto zostało zawieszone.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponseRest> handleUnauthenticated(final UnauthenticatedException ex) {
        final var error = new ErrorResponseRest("UNAUTHENTICATED", "Wymagane zalogowanie.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseRest> handleAuthentication(final AuthenticationException ex) {
        final var error = new ErrorResponseRest("UNAUTHENTICATED", "Wymagane zalogowanie.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(CsrfException.class)
    public ResponseEntity<ErrorResponseRest> handleCsrf(final CsrfException ex) {
        final var error = new ErrorResponseRest("CSRF_INVALID", "Brakujący lub niepoprawny token CSRF.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseRest> handleAccessDenied(final AccessDeniedException ex) {
        final var error = new ErrorResponseRest("ACCESS_DENIED", "Brak uprawnień do tej operacji.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseRest> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        final var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorRest(
                        fieldError.getField(),
                        fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Niepoprawna wartość"
                ))
                .toList();

        final var error = new ErrorResponseRest("VALIDATION_FAILED", "Dane nie przeszły walidacji.");
        error.setErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseRest> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex) {
        final var error = new ErrorResponseRest("MALFORMED_REQUEST", "Treść żądania nie jest poprawnym JSON-em.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseRest> handleIllegalArgument(final IllegalArgumentException ex) {
        final var error = new ErrorResponseRest("MALFORMED_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
