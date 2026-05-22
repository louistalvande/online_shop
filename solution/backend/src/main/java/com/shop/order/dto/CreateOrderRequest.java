package com.shop.order.dto;

import com.shop.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Request payload to create an order from the buyer's current cart (US-ORD-01, US-ORD-02, US-PRF-03). */
public class CreateOrderRequest {

    @Schema(description = "UUID of the delivery address from the buyer's address book (US-PRF-03)")
    @NotNull
    private UUID addressId;

    @Schema(description = "UUID of the selected carrier")
    @NotNull
    private UUID carrierId;

    @Schema(description = "CARD for immediate Stripe authorisation, WIRE_TRANSFER for bank payment")
    @NotNull
    private PaymentMethod paymentMethod;

    /** @return the delivery address UUID */
    public UUID getAddressId() { return addressId; }
    /** @param addressId the delivery address UUID */
    public void setAddressId(UUID addressId) { this.addressId = addressId; }

    /** @return the carrier UUID */
    public UUID getCarrierId() { return carrierId; }
    /** @param carrierId the carrier UUID */
    public void setCarrierId(UUID carrierId) { this.carrierId = carrierId; }

    /** @return the chosen payment method */
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    /** @param paymentMethod the chosen payment method */
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
