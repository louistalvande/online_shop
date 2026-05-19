package com.shop.order.dto;

import com.shop.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response returned immediately after order creation.
 * For CARD orders the {@code clientSecret} must be used by the frontend to confirm
 * the Stripe PaymentIntent without sending card data to this backend (PCI-DSS).
 * For WIRE_TRANSFER orders bank details are returned instead.
 */
public class CheckoutInitResponse {

    @Schema(description = "Order UUID") private UUID orderId;
    @Schema(description = "Human-readable order number") private String orderNumber;
    @Schema(description = "Chosen payment method") private PaymentMethod paymentMethod;
    @Schema(description = "Grand total including VAT") private BigDecimal totalAmountTtc;

    /** Present for CARD orders — pass to stripe.confirmCardPayment() on the frontend. */
    @Schema(description = "Stripe PaymentIntent client secret (CARD only)")
    private String clientSecret;

    /** Present for WIRE_TRANSFER orders — vendor's bank IBAN to wire funds to. */
    @Schema(description = "Vendor bank IBAN (WIRE_TRANSFER only)")
    private String bankIban;

    /** Present for WIRE_TRANSFER orders — vendor's BIC/SWIFT. */
    @Schema(description = "Vendor bank BIC (WIRE_TRANSFER only)")
    private String bankBic;

    /** Present for WIRE_TRANSFER orders — the reference the buyer must include in the wire. */
    @Schema(description = "Payment reference to include in the wire transfer (WIRE_TRANSFER only)")
    private String paymentReference;

    private CheckoutInitResponse() {}

    /**
     * Builds the response for a CARD checkout.
     *
     * @param orderId        the created order UUID
     * @param orderNumber    the human-readable order number
     * @param totalAmountTtc the grand total
     * @param clientSecret   the Stripe PaymentIntent client secret
     * @return the response DTO
     */
    public static CheckoutInitResponse forCard(UUID orderId, String orderNumber,
                                               BigDecimal totalAmountTtc, String clientSecret) {
        CheckoutInitResponse r = new CheckoutInitResponse();
        r.orderId = orderId;
        r.orderNumber = orderNumber;
        r.paymentMethod = PaymentMethod.CARD;
        r.totalAmountTtc = totalAmountTtc;
        r.clientSecret = clientSecret;
        return r;
    }

    /**
     * Builds the response for a WIRE_TRANSFER checkout.
     *
     * @param orderId        the created order UUID
     * @param orderNumber    the human-readable order number
     * @param totalAmountTtc the grand total
     * @param bankIban       the vendor's IBAN
     * @param bankBic        the vendor's BIC
     * @return the response DTO
     */
    public static CheckoutInitResponse forWire(UUID orderId, String orderNumber,
                                               BigDecimal totalAmountTtc,
                                               String bankIban, String bankBic) {
        CheckoutInitResponse r = new CheckoutInitResponse();
        r.orderId = orderId;
        r.orderNumber = orderNumber;
        r.paymentMethod = PaymentMethod.WIRE_TRANSFER;
        r.totalAmountTtc = totalAmountTtc;
        r.bankIban = bankIban;
        r.bankBic = bankBic;
        r.paymentReference = orderNumber;
        return r;
    }

    /** @return the order UUID */
    public UUID getOrderId() { return orderId; }
    /** @return the order number */
    public String getOrderNumber() { return orderNumber; }
    /** @return the payment method */
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    /** @return the grand total */
    public BigDecimal getTotalAmountTtc() { return totalAmountTtc; }
    /** @return the Stripe client secret */
    public String getClientSecret() { return clientSecret; }
    /** @return the vendor bank IBAN */
    public String getBankIban() { return bankIban; }
    /** @return the vendor bank BIC */
    public String getBankBic() { return bankBic; }
    /** @return the payment reference */
    public String getPaymentReference() { return paymentReference; }
}
