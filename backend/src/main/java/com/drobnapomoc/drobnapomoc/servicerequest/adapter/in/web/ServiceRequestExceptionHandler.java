package com.drobnapomoc.drobnapomoc.servicerequest.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.AdminServiceRequestsApiController;
import com.drobnapomoc.drobnapomoc.api.ContractorServiceRequestsApiController;
import com.drobnapomoc.drobnapomoc.api.ServiceRequestsApiController;
import com.drobnapomoc.drobnapomoc.api.model.ErrorResponseRest;
import com.drobnapomoc.drobnapomoc.api.model.FieldErrorRest;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestNotFoundException;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.exception.ServiceRequestValidationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Errors of the service request endpoints. Invalid path ids (not a number, below 1) give {@code 404},
 * like a missing request: the specs document no {@code 400} for these reads.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {
        ServiceRequestsApiController.class,
        ContractorServiceRequestsApiController.class,
        AdminServiceRequestsApiController.class
})
public class ServiceRequestExceptionHandler {

    private static final String NOT_FOUND_MESSAGE = "Nie znaleziono zgłoszenia.";

    @ExceptionHandler(ServiceRequestValidationException.class)
    public ResponseEntity<ErrorResponseRest> handleValidation(final ServiceRequestValidationException ex) {
        final var error = new ErrorResponseRest("VALIDATION_FAILED", "Dane nie przeszły walidacji.");
        error.setErrors(ex.violations().stream()
                .map(violation -> new FieldErrorRest(violation.field(), violation.message()))
                .toList());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(error);
    }

    @ExceptionHandler({
            ServiceRequestNotFoundException.class,
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponseRest> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseRest("NOT_FOUND", NOT_FOUND_MESSAGE));
    }
}
