package com.shop.order.service;

import com.shop.order.dto.OrderResponse;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-side order management: listing, wire confirmation, and wire rejection (US-VND-01..02). */
public interface VendorOrderService {

    /**
     * Returns all orders belonging to the authenticated vendor, newest first.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @return list of order responses
     */
    List<OrderResponse> getVendorOrders(String vendorEmail);

    /**
     * Returns a single order belonging to the vendor, or throws if not found.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @return the order response
     */
    OrderResponse getVendorOrder(String vendorEmail, UUID orderId);

    /**
     * Confirms receipt of a wire transfer payment: transitions the order from
     * {@code PAYMENT_PENDING_WIRE} to {@code AWAITING_PROCESSING} and notifies the buyer.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmWirePayment(String vendorEmail, UUID orderId, Locale locale);

    /**
     * Rejects a wire transfer payment: cancels the order, restores stock, and notifies the buyer.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse rejectWirePayment(String vendorEmail, UUID orderId, Locale locale);

    /**
     * Marks an order as shipped by recording the carrier tracking number and transitioning the order
     * to {@code SHIPPED}. The buyer is notified by email (US-EXP-01).
     *
     * @param vendorEmail    the authenticated vendor's email address
     * @param orderId        the order UUID
     * @param trackingNumber the carrier-issued tracking number
     * @param locale         buyer notification locale
     * @return the updated order response
     */
    OrderResponse shipOrder(String vendorEmail, UUID orderId, String trackingNumber, Locale locale);

    /**
     * Accepts a post-shipment cancellation request by requiring the buyer to return the parcel (US-CAN-03).
     * Transitions the order from {@code SHIPPED} to {@code PENDING_RETURN} and notifies the buyer.
     * For wire transfer orders, {@code buyerIban} must be provided so the refund can be processed later.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param buyerIban   the buyer's IBAN for wire refund; {@code null} for card orders
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse acceptReturn(String vendorEmail, UUID orderId, String buyerIban, Locale locale);

    /**
     * Confirms receipt of the returned parcel, triggering refund (US-CAN-03).
     * Transitions from {@code PENDING_RETURN} to {@code CANCELLED} (card) or
     * {@code WIRE_REFUND_IN_PROGRESS} (wire transfer).
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmReturn(String vendorEmail, UUID orderId, Locale locale);

    /**
     * Accepts a post-shipment cancellation without requiring a return (US-CAN-04).
     * Transitions directly from {@code SHIPPED} to {@code CANCELLED} (card) or
     * {@code WIRE_REFUND_IN_PROGRESS} (wire transfer).
     * For wire transfer orders, {@code buyerIban} must be provided.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param buyerIban   the buyer's IBAN for wire refund; {@code null} for card orders
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse waiveReturn(String vendorEmail, UUID orderId, String buyerIban, Locale locale);

    /**
     * Confirms that the wire refund has been sent to the buyer (US-CAN-05).
     * Transitions from {@code WIRE_REFUND_IN_PROGRESS} to {@code CANCELLED} and notifies the buyer.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmWireRefund(String vendorEmail, UUID orderId, Locale locale);

    /**
     * Refuses a buyer's post-shipment cancellation request (US-CAN-06).
     * Valid only when the order status is {@code CANCELLATION_REQUESTED_AFTER_SHIPMENT}.
     * Transitions back to {@code SHIPPED} and notifies the buyer of the refusal.
     *
     * @param vendorEmail the authenticated vendor's email address
     * @param orderId     the order UUID
     * @param locale      buyer notification locale
     * @return the updated order response
     */
    OrderResponse refuseCancellationRequest(String vendorEmail, UUID orderId, Locale locale);
}
