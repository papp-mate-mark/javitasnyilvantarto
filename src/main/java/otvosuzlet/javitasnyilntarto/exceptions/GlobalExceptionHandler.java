package otvosuzlet.javitasnyilntarto.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import otvosuzlet.javitasnyilntarto.model.MethodViolationErrorResponse;
import otvosuzlet.javitasnyilntarto.model.ErrorResponse;
import otvosuzlet.javitasnyilntarto.model.ErrorTypes;
import otvosuzlet.javitasnyilntarto.model.ValidationErrorResponse;
import otvosuzlet.javitasnyilntarto.model.ValidationFieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;


@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(ConstraintViolationException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        Set<ConstraintViolation<?>> result = ex.getConstraintViolations();

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(); 
        errorResponse.setErrorType(ErrorTypes.VALIDATION_ERROR);
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value()); 
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setMessage("Validation failed");
        errorResponse.setErrorKey("error.validation.failed");
        errorResponse.setErrorCode(HttpStatus.BAD_REQUEST.toString()); 

        errorResponse.setFieldErrors(result.stream()
                .map(constraintViolation -> {
                    ValidationFieldError validationFieldError = new ValidationFieldError();
                    String propertyPath = constraintViolation.getPropertyPath().toString();
                    // Extract field name by removing method name and parameter name (first 2 segments)
                    String[] pathSegments = propertyPath.split("\\.");
                    String fieldName = pathSegments.length > 2 
                        ? String.join(".", java.util.Arrays.copyOfRange(pathSegments, 2, pathSegments.length))
                        : propertyPath;
                    validationFieldError.setFieldName(fieldName);
                    validationFieldError.setMessageKey(constraintViolation.getMessage()); 
                    return validationFieldError;
                }).toList()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MethodViolationErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        MethodViolationErrorResponse errorResponse = new MethodViolationErrorResponse();
        errorResponse.setErrorType(ErrorTypes.CONSTRAINT_VIOLATION_ERROR);
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setMessage("Method violation occurred");
        errorResponse.setErrorKey("error.method.violation");
        errorResponse.setErrorCode(HttpStatus.BAD_REQUEST.toString());

        List<? extends MessageSourceResolvable> teszt = ex.getAllErrors();

        errorResponse.setViolationErrors(teszt.stream().map(error -> error.getDefaultMessage()).toList()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(BindException ex, HttpServletRequest request) {
        BindingResult result = ex.getBindingResult();

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(); 
        errorResponse.setErrorType(ErrorTypes.VALIDATION_ERROR);
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value()); 
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setMessage("Validation failed");
        errorResponse.setErrorKey("error.validation.failed");
        errorResponse.setErrorCode(HttpStatus.BAD_REQUEST.toString()); 
        List<FieldError> fieldErrors = result.getFieldErrors();

        errorResponse.setFieldErrors(fieldErrors.stream()
                .map(fieldError -> {
                    ValidationFieldError validationFieldError = new ValidationFieldError();
                    validationFieldError.setFieldName(fieldError.getField());
                    validationFieldError.setMessageKey(fieldError.getDefaultMessage()); 
                    return validationFieldError;
                }).toList()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(RuntimeExceptionWithCode.class)
    public ResponseEntity<ErrorResponse> handleJobNotFound(RuntimeExceptionWithCode e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorType(ErrorTypes.GENERAL_ERROR);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(e.getStatus().value());
        errorResponse.setErrorKey(e.getErrorCode());
        errorResponse.setErrorCode(e.getStatus().toString());
        
        errorResponse.setMessage(e.getMessage());
        errorResponse.setPath(request.getRequestURI());


        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorType(ErrorTypes.GENERAL_ERROR);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setErrorKey("error.invalid.credentials");
        errorResponse.setErrorCode(HttpStatus.UNAUTHORIZED.toString());
        errorResponse.setMessage("Invalid credentials provided");
        errorResponse.setPath(request.getRequestURI());


        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorType(ErrorTypes.GENERAL_ERROR);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
        errorResponse.setErrorKey("error.access.denied");
        errorResponse.setErrorCode(HttpStatus.FORBIDDEN.toString());
        errorResponse.setMessage("Access denied");
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}
