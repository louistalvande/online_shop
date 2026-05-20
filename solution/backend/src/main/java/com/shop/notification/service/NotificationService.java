package com.shop.notification.service;

import com.shop.claim.dto.ClaimResponse;
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
     * Notifies a vendor by email that a new order has been placed (US-ORD-03, US-ORD-04).
     *
     * @param toEmail the vendor's email address
     * @param order   the new order
     * @param locale  the locale used to select the email language
     */
    void sendVendorNewOrderEmail(String toEmail, OrderResponse order, Locale locale);

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
     * Notifies the vendor that the buyer has cancelled an order before shipment (US-CAN-02).
     *
     * @param toEmail the vendor's email address
     * @param order   the cancelled order
     * @param locale  the locale used to select the email language
     */
    void sendVendorCancellationEmail(String toEmail, OrderResponse order, Locale locale);

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
     * Notifies the vendor that a buyer has opened a new claim (US-CLM-01).
     *
     * @param toEmail the vendor's email address
     * @param claim   the newly opened claim
     * @param locale  the locale used to select the email language
     */
    void sendClaimOpenedEmail(String toEmail, ClaimResponse claim, Locale locale);

    /**
     * Notifies the buyer that the vendor has granted a refund for their claim (US-CLM-02).
     *
     * @param toEmail the buyer's email address
     * @param claim   the closed claim with decision GRANTED
     * @param locale  the locale used to select the email language
     */
    void sendClaimGrantedEmail(String toEmail, ClaimResponse claim, Locale locale);

    /**
     * Notifies the buyer that the vendor has refused their claim (US-CLM-02).
     *
     * @param toEmail the buyer's email address
     * @param claim   the closed claim with decision REFUSED
     * @param locale  the locale used to select the email language
     */
    void sendClaimRefusedEmail(String toEmail, ClaimResponse claim, Locale locale);
}
