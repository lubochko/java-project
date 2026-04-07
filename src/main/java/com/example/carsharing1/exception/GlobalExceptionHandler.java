package com.example.carsharing1.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BAD_REQUEST = "Bad Request";
    private static final String NOT_FOUND = "Not Found";
    private static final String CONFLICT = "Conflict";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String VALIDATION_FAILED = "Validation Failed";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.error("Ошибка валидации: {}", ex.getMessage());

        Map<String, List<String>> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new java.util.ArrayList<>()).add(errorMessage);
        });

        ValidationErrorResponse response = new ValidationErrorResponse(
                java.time.LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                VALIDATION_FAILED,
                "Проверьте правильность введенных данных",
                request.getDescription(false).replace("uri=", ""),
                fieldErrors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        log.error("Ошибка типа параметра: {}", ex.getMessage());

        if (ex.getRequiredType() == null) {
            ErrorResponse response = ErrorResponse.of(
                    HttpStatus.BAD_REQUEST.value(),
                    BAD_REQUEST,
                    "Параметр '" + ex.getName() + "' имеет некорректный тип",
                    request.getDescription(false).replace("uri=", "")
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                BAD_REQUEST,
                "Параметр '" + ex.getName() + "' должен быть типа " + ex.getRequiredType().getSimpleName(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.error("Ресурс не найден: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                NOT_FOUND,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            WebRequest request) {
        log.error("Дубликат ресурса: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                CONFLICT,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
            DuplicateEmailException ex,
            WebRequest request) {
        log.error("Дубликат email: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                CONFLICT,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateLicenseException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLicenseException(
            DuplicateLicenseException ex,
            WebRequest request) {
        log.error("Дубликат водительских прав: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                CONFLICT,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CarServiceException.class)
    public ResponseEntity<ErrorResponse> handleCarServiceException(
            CarServiceException ex,
            WebRequest request) {
        log.error("Ошибка сервиса машин: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ErrorResponse> handleBookingException(
            BookingException ex,
            WebRequest request) {
        log.error("Ошибка бронирования: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        log.error("Необработанная ошибка: ", ex);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR,
                "Произошла внутренняя ошибка сервера",
                request.getDescription(false).replace("uri=", ""),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}