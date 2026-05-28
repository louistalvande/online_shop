package com.shop.order.service;

import com.shop.order.dto.OrderResponse;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-side order management (US-VND-01..02). */
public interface VendorOrderService {

    /**
     * Returns all orders, newest first.
     *
     * @return list of order responses
     */
    List<OrderResponse> getVendorOrders();

    /**
     * Returns a single order by ID.
     *
     * @param orderId the order UUID
     * @return the order response
     */
    OrderResponse getVendorOrder(UUID orderId);

    /**
     * Confirms receipt of a wire transfer payment: transitions the order from
     * {@code PAYMENT_PENDING_WIRE} to {@code AWAITING_PROCESSING} and notifies the buyer.
     *
     * @param orderId the order UUID
     * @param locale  buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmWirePayment(UUID orderId, Locale locale);

    /**
     * Rejects a wire transfer payment: cancels the order, restores stock, and notifies the buyer.
     *
     * @param orderId the order UUID
     * @param locale  buyer notification locale
     * @return the updated order response
     */
    OrderResponse rejectWirePayment(UUID orderId, Locale locale);

    /**
     * Marks an order as in preparation: transitions it from {@code AWAITING_PROCESSING}
     * to {@code IN_PREPARATION} and notifies the buyer (US-VND-01).
     *
     * @param orderId the order UUID
     * @param locale  buyer notification locale
     * @return the updated order response
     */
    OrderResponse markOrderInPreparation(UUID orderId, Locale locale);

    /**
     * Marks an order as shipped by recording the carrier tracking number and transitioning the order
     * to {@code SHIPPED}. The buyer is notified by email (US-EXP-01).
     *
     * @param orderId        the order UUID
     * @param trackingNumber the carrier-issued tracking number
     * @param locale         buyer notification locale
     * @return the updated order response
     */
    OrderResponse shipOrder(UUID orderId, String trackingNumber, Locale locale);

    /**
     * Accepts a post-shipment cancellation request by requiring the buyer to return the parcel (US-CAN-03).
     * Transitions the order from {@code SHIPPED} to {@code PENDING_RETURN} and notifies the buyer.
     * For wire transfer orders, {@code buyerIban} must be provided.
     *
     * @param orderId   the order UUID
     * @param buyerIban the buyer's IBAN for wire refund; {@code null} for card orders
     * @param locale    buyer notification locale
     * @return the updated order response
     */
    OrderResponse acceptReturn(UUID orderId, String buyerIban, Locale locale);

    /**
     * Confirms receipt of the returned parcel, triggering refund (US-CAN-03).
     *
     * @param orderId the order UUID
     * @param locale  buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmReturn(UUID orderId, Locale locale);

    /**
     * Accepts a post-shipment cancellation without requiring a return (US-CAN-04).
     *
     * @param orderId   the order UUID
     * @param buyerIban the buyer's IBAN for wire refund; {@code null} for card orders
     * @param locale    buyer notification locale
     * @return the updated order response
     */
    OrderResponse waiveReturn(UUID orderId, String buyerIban, Locale locale);

    /**
     * Confirms that the wire refund has been sent to the buyer (US-CAN-05).
     *
     * @param orderId the order UUID
     * @param locale  buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmWireRefund(UUID orderId, Locale locale);

    /**
     * Refuses a buyer's post-shipment cancellation request (US-CAN-06).
     *
     * @param orderId the order UUID
     * @param locale  buyer notification locale
     * @return the updated order response
     */
    OrderResponse refuseCancellationRequest(UUID orderId, Locale locale);
}
