package com.shop.order.dto;

import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Full order response DTO. */
public class OrderResponse {

    @Schema(description = "Order UUID") private UUID id;
    @Schema(description = "Human-readable order number") private String orderNumber;
    @Schema(description = "Buyer account UUID") private UUID buyerId;
    @Schema(description = "Carrier UUID") private UUID carrierId;
    @Schema(description = "Carrier name snapshot") private String carrierName;
    @Schema(description = "Carrier tracking URL snapshot") private String carrierTrackingUrl;
    @Schema(description = "Delivery street address") private String deliveryAddressLine;
    @Schema(description = "Delivery city") private String deliveryCity;
    @Schema(description = "Delivery postal code") private String deliveryPostalCode;
    @Schema(description = "Delivery country ISO code") private String deliveryCountryCode;
    @Schema(description = "Payment method") private PaymentMethod paymentMethod;
    @Schema(description = "Current order status") private OrderStatus status;
    @Schema(description = "Grand total including VAT") private BigDecimal totalAmountTtc;
    @Schema(description = "Shipment tracking number, null until shipped") private String trackingNumber;
    @Schema(description = "Buyer IBAN for wire refund, populated when provided") private String buyerIban;
    @Schema(description = "Reason given by the buyer when requesting post-shipment cancellation (US-CAN-06)") private String cancellationReason;
    @Schema(description = "Order line items") private List<OrderLineResponse> lines;
    @Schema(description = "Order creation timestamp") private LocalDateTime createdAt;
    @Schema(description = "Last status-change timestamp") private LocalDateTime updatedAt;

    private OrderResponse() {}

    /**
     * Builds a response from an {@link Order} entity.
     *
     * @param order the order entity
     * @return the populated response DTO
     */
    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.orderNumber = order.getOrderNumber();
        r.buyerId = order.getBuyerId();
        r.carrierId = order.getCarrierId();
        r.carrierName = order.getCarrierName();
        r.carrierTrackingUrl = order.getCarrierTrackingUrl();
        r.deliveryAddressLine = order.getDeliveryAddressLine();
        r.deliveryCity = order.getDeliveryCity();
        r.deliveryPostalCode = order.getDeliveryPostalCode();
        r.deliveryCountryCode = order.getDeliveryCountryCode();
        r.paymentMethod = order.getPaymentMethod();
        r.status = order.getStatus();
        r.totalAmountTtc = order.getTotalAmountTtc();
        r.trackingNumber = order.getTrackingNumber();
        r.buyerIban = order.getBuyerIban();
        r.cancellationReason = order.getCancellationReason();
        r.lines = order.getLines().stream().map(OrderLineResponse::from).toList();
        r.createdAt = order.getCreatedAt();
        r.updatedAt = order.getUpdatedAt();
        return r;
    }

    /** @return the order UUID */
    public UUID getId() { return id; }
    /** @return the human-readable order number */
    public String getOrderNumber() { return orderNumber; }
    /** @return the buyer account UUID */
    public UUID getBuyerId() { return buyerId; }
    /** @return the carrier UUID */
    public UUID getCarrierId() { return carrierId; }
    /** @return the carrier name */
    public String getCarrierName() { return carrierName; }
    /** @return the carrier tracking URL */
    public String getCarrierTrackingUrl() { return carrierTrackingUrl; }
    /** @return the delivery address */
    public String getDeliveryAddressLine() { return deliveryAddressLine; }
    /** @return the delivery city */
    public String getDeliveryCity() { return deliveryCity; }
    /** @return the delivery postal code */
    public String getDeliveryPostalCode() { return deliveryPostalCode; }
    /** @return the delivery country code */
    public String getDeliveryCountryCode() { return deliveryCountryCode; }
    /** @return the payment method */
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    /** @return the current order status */
    public OrderStatus getStatus() { return status; }
    /** @return the grand total including VAT */
    public BigDecimal getTotalAmountTtc() { return totalAmountTtc; }
    /** @return the tracking number, or null */
    public String getTrackingNumber() { return trackingNumber; }
    /** @return the buyer IBAN for wire refund, or null */
    public String getBuyerIban() { return buyerIban; }
    /** @return the buyer's cancellation reason, or null */
    public String getCancellationReason() { return cancellationReason; }
    /** @return the order lines */
    public List<OrderLineResponse> getLines() { return lines; }
    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /** @return the last-updated timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
