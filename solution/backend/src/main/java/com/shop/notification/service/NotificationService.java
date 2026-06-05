package com.shop.notification.service;

import com.shop.order.dto.OrderResponse;

import java.math.BigDecimal;
import java.util.Locale;

/** Sends transactional emails to platform users. */
public interface NotificationService {

    /**
     * Sends an activation email containing the link to set a password and activate the account.
     * Used for both admin-created accounts (US-ADM-01) and buyer self-registration (US-REG-01).
     *
     * @param toEmail        the recipient email address
     * @param activationLink the full activation URL including the token
     * @param locale         the locale used to select the email language
     */
    void sendActivationEmail(String toEmail, String activationLink, Locale locale);

    /**
     * Sends an order confirmation email to the buyer after successful payment (US-ORD-05).
     *
     * @param toEmail the buyer's email address
     * @param order   the confirmed order
     * @param locale  the locale used to select the email language
     */
    void sendOrderConfirmationEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Sends wire transfer bank details to the buyer for a pending wire payment (US-ORD-04).
     *
     * @param toEmail        the buyer's email address
     * @param orderNumber    the human-readable order number (used as payment reference)
     * @param totalAmountTtc the amount to transfer
     * @param bankIban       the vendor's IBAN
     * @param bankBic        the vendor's BIC
     * @param locale         the locale used to select the email language
     */
    void sendWireTransferDetailsEmail(String toEmail, String orderNumber,
                                      BigDecimal totalAmountTtc,
                                      String bankIban, String bankBic,
                                      Locale locale);

    /**
     * Notifies the buyer that the vendor has rejected their wire transfer payment and the order is cancelled (US-VND-02).
     *
     * @param toEmail the buyer's email address
     * @param order   the cancelled order
     * @param locale  the locale used to select the email language
     */
    void sendWirePaymentRejectedEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies the buyer that their order has been shipped with the tracking number (US-EXP-01).
     *
     * @param toEmail the buyer's email address
     * @param order   the shipped order (includes tracking number and carrier URL)
     * @param locale  the locale used to select the email language
     */
    void sendShipmentNotificationEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies the buyer that their cancellation request has been processed (US-CAN-01).
     *
     * @param toEmail the buyer's email address
     * @param order   the cancelled or wire-refund-pending order
     * @param locale  the locale used to select the email language
     */
    void sendBuyerCancellationEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Sends a password-reset email containing the one-time recovery link (SEC-PWD-006 / CPA-17).
     * Callers must not reveal whether the address exists — always call this silently.
     *
     * @param toEmail   the recipient email address
     * @param resetLink the full reset URL including the one-time token
     * @param locale    the locale used to select the email language
     */
    void sendPasswordResetEmail(String toEmail, String resetLink, Locale locale);

    /**
     * Notifies the buyer that they must return the parcel before the refund is issued (US-CAN-03).
     *
     * @param toEmail the buyer's email address
     * @param order   the order now in PENDING_RETURN status
     * @param locale  the locale used to select the email language
     */
    void sendReturnRequestedEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies the buyer that the wire refund has been sent by the vendor (US-CAN-05).
     *
     * @param toEmail the buyer's email address
     * @param order   the now-cancelled order
     * @param locale  the locale used to select the email language
     */
    void sendWireRefundConfirmedEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies a buyer that an out-of-stock product they subscribed to is back in stock (US-SHP-03 / FS-B14).
     *
     * @param toEmail     the buyer's email address
     * @param productName the name of the product now available
     * @param locale      the locale used to select the email language
     */
    void sendBackInStockEmail(String toEmail, String productName, Locale locale);

    /**
     * Notifies the buyer that the vendor has refused their post-shipment cancellation request (US-CAN-06).
     *
     * @param toEmail the buyer's email address
     * @param order   the order back in SHIPPED status
     * @param locale  the locale used to select the email language
     */
    void sendCancellationRefusedEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies the buyer that their order is now being prepared by the vendor (US-VND-01).
     *
     * @param toEmail the buyer's email address
     * @param order   the order now in IN_PREPARATION status
     * @param locale  the locale used to select the email language
     */
    void sendOrderInPreparationEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies a vendor that a new order has been placed and awaits processing (US-ORD-05).
     * Called for all vendor accounts so every vendor is aware of incoming orders.
     *
     * @param toEmail the vendor's email address
     * @param order   the newly placed order
     * @param locale  the locale used to select the email language
     */
    void sendVendorNewOrderEmail(String toEmail, OrderResponse order, Locale locale);

    /**
     * Notifies an account holder that their password has been administratively revoked and they
     * must reset it immediately (US-SEC-04 / FS-S11 / CPA-17).
     *
     * @param toEmail   the account's email address
     * @param resetLink the password-reset URL to include in the email
     * @param locale    the locale used to select the email language
     */
    void sendPasswordRevokedEmail(String toEmail, String resetLink, Locale locale);

    /**
     * Sends a promotional marketing campaign email to a single buyer (US-MKTG-01 / FS-V17).
     * The subject and body are composed by the vendor and sent as-is (not localised via templates).
     *
     * @param toEmail  the buyer's email address
     * @param subject  the campaign subject line
     * @param body     the campaign body text
     * @param locale   the buyer's preferred locale (used only if the implementation falls back to templates)
     */
    void sendMarketingCampaignEmail(String toEmail, String subject, String body, Locale locale);

}
