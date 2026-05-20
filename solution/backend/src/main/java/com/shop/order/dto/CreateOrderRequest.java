package com.shop.order.dto;

import com.shop.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Request payload to create an order from the buyer's current cart (US-ORD-01, US-ORD-02). */
public class CreateOrderRequest {

    @Schema(description = "Street address of the delivery destination")
    @NotBlank
    @Size(max = 255)
    private String deliveryAddressLine;

    @Schema(description = "City of the delivery destination")
    @NotBlank
    @Size(max = 100)
    private String deliveryCity;

    @Schema(description = "Postal code of the delivery destination")
    @NotBlank
    @Size(max = 20)
    private String deliveryPostalCode;

    @Schema(description = "ISO 3166-1 alpha-2 country code — must be a Eurozone country (CS-04)")
    @NotBlank
    @Pattern(regexp = "[A-Z]{2}", message = "{error.country.code.invalid}")
    private String deliveryCountryCode;

    @Schema(description = "UUID of the selected carrier")
    @NotNull
    private UUID carrierId;

    @Schema(description = "CARD for immediate Stripe authorisation, WIRE_TRANSFER for bank payment")
    @NotNull
    private PaymentMethod paymentMethod;

    /** @return the delivery street address */
    public String getDeliveryAddressLine() { return deliveryAddressLine; }
    /** @param deliveryAddressLine the delivery street address */
    public void setDeliveryAddressLine(String deliveryAddressLine) { this.deliveryAddressLine = deliveryAddressLine; }

    /** @return the delivery city */
    public String getDeliveryCity() { return deliveryCity; }
    /** @param deliveryCity the delivery city */
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

    /** @return the delivery postal code */
    public String getDeliveryPostalCode() { return deliveryPostalCode; }
    /** @param deliveryPostalCode the delivery postal code */
    public void setDeliveryPostalCode(String deliveryPostalCode) { this.deliveryPostalCode = deliveryPostalCode; }

    /** @return the ISO alpha-2 country code */
    public String getDeliveryCountryCode() { return deliveryCountryCode; }
    /** @param deliveryCountryCode the ISO alpha-2 country code */
    public void setDeliveryCountryCode(String deliveryCountryCode) { this.deliveryCountryCode = deliveryCountryCode; }

    /** @return the carrier UUID */
    public UUID getCarrierId() { return carrierId; }
    /** @param carrierId the carrier UUID */
    public void setCarrierId(UUID carrierId) { this.carrierId = carrierId; }

    /** @return the chosen payment method */
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    /** @param paymentMethod the chosen payment method */
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
