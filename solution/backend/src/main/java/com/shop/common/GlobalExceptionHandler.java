package com.shop.common;

import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.auth.exception.InvalidActivationTokenException;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.PasswordsMismatchException;
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
     * Handles account not found — returns HTTP 404.
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFound(
            AccountNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.account.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "ACCOUNT_NOT_FOUND", "message", message));
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
     * Handles invalid login credentials — returns HTTP 401 with a generic error (CS-08).
     * The message is deliberately vague to prevent account enumeration.
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 401 response with a localised error body
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
            InvalidCredentialsException ex, Locale locale) {
        String message = messageSource.getMessage("error.invalidCredentials", null, locale);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "INVALID_CREDENTIALS", "message", message));
    }

    /**
     * Handles expired or unknown activation token — returns HTTP 410 Gone (CS-07).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 410 response with a localised error body
     */
    @ExceptionHandler(InvalidActivationTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidActivationToken(
            InvalidActivationTokenException ex, Locale locale) {
        String message = messageSource.getMessage("error.activation.token.invalid", null, locale);
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "TOKEN_EXPIRED", "message", message));
    }

    @ExceptionHandler(PasswordsMismatchException.class)
    public ResponseEntity<Map<String, String>> handlePasswordsMismatch(
            PasswordsMismatchException ex, Locale locale) {
        String message = messageSource.getMessage("error.passwords.mismatch", null, ex.getMessage(), locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "PASSWORDS_MISMATCH", "message", message));
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
