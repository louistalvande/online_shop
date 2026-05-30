package com.shop.common;

import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.DeliveryAddressNotFoundException;
import com.shop.account.exception.UnsupportedBannerImageTypeException;
import com.shop.account.exception.UnsupportedLogoImageTypeException;
import com.shop.announcement.exception.AnnouncementNotFoundException;
import com.shop.announcement.exception.UnsupportedImageTypeException;
import com.shop.account.exception.LastActiveAddressException;
import com.shop.report.exception.InvalidPeriodException;
import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.account.exception.InvalidAccountStateException;
import com.shop.account.exception.WrongCurrentPasswordException;
import com.shop.auth.exception.InvalidActivationTokenException;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.InvalidMfaCodeException;
import com.shop.auth.exception.InvalidResetTokenException;
import com.shop.auth.exception.PasswordCompromisedException;
import com.shop.auth.exception.PasswordsMismatchException;
import com.shop.auth.exception.TokenNotFoundException;
import com.shop.auth.exception.TooManyLoginAttemptsException;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductOutOfStockException;
import com.shop.carrier.exception.CarrierNotFoundException;
import com.shop.catalog.exception.AlreadySubscribedException;
import com.shop.catalog.exception.CsvHeaderInvalidException;
import com.shop.catalog.exception.ProductArchivedConflictException;
import com.shop.catalog.exception.ProductInStockException;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.exception.StockSubscriptionNotFoundException;
import com.shop.catalog.exception.UnsupportedProductImageTypeException;
import com.shop.order.exception.CarrierNotAvailableException;
import com.shop.order.exception.EmptyCartException;
import com.shop.order.exception.InvalidDeliveryCountryException;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.MissingBuyerIbanException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.exception.PaymentFailedException;
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
     * Handles delivery address not found or not owned by the buyer — returns HTTP 404 (US-PRF-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(DeliveryAddressNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleDeliveryAddressNotFound(
            DeliveryAddressNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.address.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "ADDRESS_NOT_FOUND", "message", message));
    }

    /**
     * Handles attempt to delete the last active delivery address — returns HTTP 409 (US-PRF-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(LastActiveAddressException.class)
    public ResponseEntity<Map<String, String>> handleLastActiveAddress(
            LastActiveAddressException ex, Locale locale) {
        String message = messageSource.getMessage("error.address.last.active", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "LAST_ACTIVE_ADDRESS", "message", message));
    }

    /**
     * Handles carrier not found — returns HTTP 404.
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(CarrierNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCarrierNotFound(
            CarrierNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.carrier.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "CARRIER_NOT_FOUND", "message", message));
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
     * Handles invalid account state transitions (suspend/reactivate on wrong status) — returns HTTP 409.
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(InvalidAccountStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAccountState(
            InvalidAccountStateException ex, Locale locale) {
        String message = messageSource.getMessage("error.account.invalid.state", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INVALID_ACCOUNT_STATE", "message", message));
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

    /**
     * Handles a missing activation token (already used or never issued) — returns HTTP 404 (CS-07).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with error code TOKEN_NOT_FOUND
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTokenNotFound(
            TokenNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.activation.token.notfound", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "TOKEN_NOT_FOUND", "message", message));
    }

    /**
     * Handles wrong current password on profile update — returns HTTP 422 (US-PRF-01, US-PRF-02).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 422 response with a localised error body
     */
    @ExceptionHandler(WrongCurrentPasswordException.class)
    public ResponseEntity<Map<String, String>> handleWrongCurrentPassword(
            WrongCurrentPasswordException ex, Locale locale) {
        String message = messageSource.getMessage("error.password.wrong", null, locale);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "WRONG_PASSWORD", "message", message));
    }

    @ExceptionHandler(PasswordsMismatchException.class)
    public ResponseEntity<Map<String, String>> handlePasswordsMismatch(
            PasswordsMismatchException ex, Locale locale) {
        String message = messageSource.getMessage("error.passwords.mismatch", null, ex.getMessage(), locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "PASSWORDS_MISMATCH", "message", message));
    }

    /**
     * Handles too many login attempts (account temporarily locked) — returns HTTP 429 (SEC-AUTH-003).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 429 response with a localised error body
     */
    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<Map<String, String>> handleTooManyAttempts(
            TooManyLoginAttemptsException ex, Locale locale) {
        String message = messageSource.getMessage("error.too.many.attempts", null, locale);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "TOO_MANY_ATTEMPTS", "message", message));
    }

    /**
     * Handles product not found — returns HTTP 404.
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(
            ProductNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.product.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "PRODUCT_NOT_FOUND", "message", message));
    }

    /**
     * Handles archiving conflict (product referenced in an active order) — returns HTTP 409 (US-CAT-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(ProductArchivedConflictException.class)
    public ResponseEntity<Map<String, String>> handleProductArchivedConflict(
            ProductArchivedConflictException ex, Locale locale) {
        String message = messageSource.getMessage("error.product.archive.conflict", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "PRODUCT_ARCHIVE_CONFLICT", "message", message));
    }

    /**
     * Handles an invalid or missing CSV header on product import — returns HTTP 400 (US-CAT-06).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(CsvHeaderInvalidException.class)
    public ResponseEntity<Map<String, String>> handleCsvHeaderInvalid(
            CsvHeaderInvalidException ex, Locale locale) {
        String message = messageSource.getMessage("error.csv.header.invalid", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "CSV_HEADER_INVALID", "message", message));
    }

    /**
     * Handles cart item not found — returns HTTP 404 (US-CRT-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartItemNotFound(
            CartItemNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.cart.item.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "CART_ITEM_NOT_FOUND", "message", message));
    }

    /**
     * Handles out-of-stock product on cart add or quantity update — returns HTTP 409 (US-CRT-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleProductOutOfStock(
            ProductOutOfStockException ex, Locale locale) {
        String message = messageSource.getMessage("error.product.out.of.stock", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "PRODUCT_OUT_OF_STOCK", "message", message));
    }

    /**
     * Handles checkout with empty cart — returns HTTP 400 (US-ORD-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<Map<String, String>> handleEmptyCart(
            EmptyCartException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.empty.cart", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "EMPTY_CART", "message", message));
    }

    /**
     * Handles non-Eurozone delivery country — returns HTTP 422 (US-ORD-01, CS-04).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 422 response with a localised error body
     */
    @ExceptionHandler(InvalidDeliveryCountryException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDeliveryCountry(
            InvalidDeliveryCountryException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.invalid.country", null, locale);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "INVALID_DELIVERY_COUNTRY", "message", message));
    }

    /**
     * Handles carrier unavailable for the delivery country — returns HTTP 409 (US-ORD-02).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(CarrierNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleCarrierNotAvailable(
            CarrierNotAvailableException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.carrier.not.available", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "CARRIER_NOT_AVAILABLE", "message", message));
    }

    /**
     * Handles order not found or not owned by the buyer — returns HTTP 404 (US-ORD-03..05).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound(
            OrderNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "ORDER_NOT_FOUND", "message", message));
    }

    /**
     * Handles an operation on an order in an incompatible status — returns HTTP 409.
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOrderState(
            InvalidOrderStateException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.invalid.state", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "INVALID_ORDER_STATE", "message", message));
    }

    /**
     * Handles a failed or declined Stripe card payment — returns HTTP 402 (US-ORD-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 402 response with a localised error body
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, String>> handlePaymentFailed(
            PaymentFailedException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.payment.failed", null, locale);
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of("error", "PAYMENT_FAILED", "message", message));
    }

    /**
     * Handles missing buyer IBAN when cancelling a wire transfer order — returns HTTP 422 (US-CAN-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 422 response with a localised error body
     */
    @ExceptionHandler(MissingBuyerIbanException.class)
    public ResponseEntity<Map<String, String>> handleMissingBuyerIban(
            MissingBuyerIbanException ex, Locale locale) {
        String message = messageSource.getMessage("error.order.missing.buyer.iban", null, locale);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "MISSING_BUYER_IBAN", "message", message));
    }

    /**
     * Handles announcement not found or not owned by the authenticated vendor — returns HTTP 404 (US-ANN-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(AnnouncementNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAnnouncementNotFound(
            AnnouncementNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.announcement.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "ANNOUNCEMENT_NOT_FOUND", "message", message));
    }

    /**
     * Handles upload of an unsupported image type — returns HTTP 400 (US-ANN-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(UnsupportedImageTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedImageType(
            UnsupportedImageTypeException ex, Locale locale) {
        String message = messageSource.getMessage("error.announcement.image.type", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "UNSUPPORTED_IMAGE_TYPE", "message", message));
    }

    /**
     * Handles upload of an unsupported image type for a product photo — returns HTTP 400 (US-CAT-09).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(UnsupportedProductImageTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedProductImageType(
            UnsupportedProductImageTypeException ex, Locale locale) {
        String message = messageSource.getMessage("error.product.image.type", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "UNSUPPORTED_IMAGE_TYPE", "message", message));
    }

    /**
     * Handles upload of an unsupported image type for the vendor logo — returns HTTP 400 (BES-VND-015).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(UnsupportedLogoImageTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedLogoImageType(
            UnsupportedLogoImageTypeException ex, Locale locale) {
        String message = messageSource.getMessage("error.vendor.logo.image.type", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "UNSUPPORTED_IMAGE_TYPE", "message", message));
    }

    /**
     * Handles upload of an unsupported image type for the vendor banner — returns HTTP 400 (BES-VND-015).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(UnsupportedBannerImageTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedBannerImageType(
            UnsupportedBannerImageTypeException ex, Locale locale) {
        String message = messageSource.getMessage("error.vendor.banner.image.type", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "UNSUPPORTED_IMAGE_TYPE", "message", message));
    }

    /**
     * Handles an invalid sales report period format — returns HTTP 400 (US-RPT-01).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 400 response with a localised error body
     */
    @ExceptionHandler(InvalidPeriodException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPeriod(
            InvalidPeriodException ex, Locale locale) {
        String message = messageSource.getMessage("error.report.invalid.period", null, locale);
        return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_PERIOD", "message", message));
    }

    /**
     * Handles a password found in the HIBP compromised-password database — returns HTTP 422 (SEC-PWD-002).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 422 response with a localised error body
     */
    @ExceptionHandler(PasswordCompromisedException.class)
    public ResponseEntity<Map<String, String>> handlePasswordCompromised(
            PasswordCompromisedException ex, Locale locale) {
        String message = messageSource.getMessage("error.password.compromised", null, locale);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "PASSWORD_COMPROMISED", "message", message));
    }

    /**
     * Handles an expired, unknown, or already-used password-reset token — returns HTTP 410 (SEC-PWD-006).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 410 response with a localised error body
     */
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResetToken(
            InvalidResetTokenException ex, Locale locale) {
        String message = messageSource.getMessage("error.reset.token.invalid", null, locale);
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "RESET_TOKEN_INVALID", "message", message));
    }

    /**
     * Handles an invalid or expired TOTP code during MFA verification — returns HTTP 401 (SEC-AUTH-007).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 401 response with a localised error body
     */
    @ExceptionHandler(InvalidMfaCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMfaCode(
            InvalidMfaCodeException ex, Locale locale) {
        String message = messageSource.getMessage("error.mfa.code.invalid", null, locale);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "INVALID_MFA_CODE", "message", message));
    }

    /**
     * Handles an attempt to subscribe when a subscription already exists — returns HTTP 409 (US-SHP-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(AlreadySubscribedException.class)
    public ResponseEntity<Map<String, String>> handleAlreadySubscribed(
            AlreadySubscribedException ex, Locale locale) {
        String message = messageSource.getMessage("error.subscription.already.exists", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "ALREADY_SUBSCRIBED", "message", message));
    }

    /**
     * Handles an attempt to subscribe to a product that is in stock — returns HTTP 409 (US-SHP-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 409 response with a localised error body
     */
    @ExceptionHandler(ProductInStockException.class)
    public ResponseEntity<Map<String, String>> handleProductInStock(
            ProductInStockException ex, Locale locale) {
        String message = messageSource.getMessage("error.subscription.product.in.stock", null, locale);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "PRODUCT_IN_STOCK", "message", message));
    }

    /**
     * Handles cancellation of a subscription that does not exist — returns HTTP 404 (US-SHP-03).
     *
     * @param ex     the exception
     * @param locale the request locale
     * @return a 404 response with a localised error body
     */
    @ExceptionHandler(StockSubscriptionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleStockSubscriptionNotFound(
            StockSubscriptionNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage("error.subscription.not.found", null, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "SUBSCRIPTION_NOT_FOUND", "message", message));
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
