package com.drobnapomoc.drobnapomoc.auth.web;

import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.FieldErrorRest;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.AccountSuspendedException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.EmailAlreadyTakenException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.InvalidCredentialsException;
import com.drobnapomoc.drobnapomoc.auth.domain.exception.UnauthenticatedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponseRest> handleEmailAlreadyTaken(EmailAlreadyTakenException ex) {
        var error = new ErrorResponseRest("EMAIL_TAKEN", "Konto z tym adresem email już istnieje.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseRest> handleInvalidCredentials(InvalidCredentialsException ex) {
        var error = new ErrorResponseRest("INVALID_CREDENTIALS", "Niepoprawny email lub hasło.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ErrorResponseRest> handleAccountSuspended(AccountSuspendedException ex) {
        var error = new ErrorResponseRest("ACCOUNT_SUSPENDED", "Konto zostało zawieszone.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponseRest> handleUnauthenticated(UnauthenticatedException ex) {
        var error = new ErrorResponseRest("UNAUTHENTICATED", "Wymagane zalogowanie.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseRest> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<FieldErrorRest> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new FieldErrorRest(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Niepoprawna wartość"
            ));
        }

        var error = new ErrorResponseRest("VALIDATION_FAILED", "Dane nie przeszły walidacji.");
        error.setErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseRest> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        var error = new ErrorResponseRest("MALFORMED_REQUEST", "Treść żądania nie jest poprawnym JSON-em.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseRest> handleIllegalArgument(IllegalArgumentException ex) {
        var error = new ErrorResponseRest("MALFORMED_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
