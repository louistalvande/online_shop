package com.shop.common;

import com.shop.account.exception.EmailAlreadyUsedException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.Map;

/** Maps domain exceptions to structured HTTP error responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Constructs the handler with the application message source for i18n.
     *
     * @param messageSource Spring message source for localised messages
     */
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handles duplicate email on account creation — returns HTTP 409.
     *
     * @param ex     the exception
     * @param locale the request locale, used to resolve the error message
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyUsed(
            EmailAlreadyUsedException ex, Locale locale) {
        String message = messageSource.getMessage("error.email.already.used", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "EMAIL_ALREADY_USED", "message", message));
    }

    /**
     * Handles Bean Validation failures — returns HTTP 400 with the first violation message.
     *
     * @param ex     the validation exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex, Locale locale) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(err -> messageSource.getMessage(err, locale))
                .orElse("Validation error");
        return ResponseEntity.badRequest()
                .body(Map.of("error", "VALIDATION_ERROR", "message", message));
    }
}
