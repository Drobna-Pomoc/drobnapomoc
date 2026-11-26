package com.drobnapomoc.drobnapomoc.auth.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthExceptionHandler {

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

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponseRest> handleUnauthenticated() {
        return errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Wymagane zalogowanie.");
    }

    private ResponseEntity<ErrorResponseRest> errorResponse(
            final HttpStatus status,
            final String code,
            final String message
    ) {
        return ResponseEntity.status(status).body(new ErrorResponseRest(code, message));
    }
}
