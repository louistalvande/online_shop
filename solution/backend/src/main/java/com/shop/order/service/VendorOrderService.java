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
     * @param vendorId the authenticated vendor's account UUID
     * @return list of order responses
     */
    List<OrderResponse> getVendorOrders(UUID vendorId);

    /**
     * Returns a single order belonging to the vendor, or throws if not found.
     *
     * @param vendorId the authenticated vendor's account UUID
     * @param orderId  the order UUID
     * @return the order response
     */
    OrderResponse getVendorOrder(UUID vendorId, UUID orderId);

    /**
     * Confirms receipt of a wire transfer payment: transitions the order from
     * {@code PAYMENT_PENDING_WIRE} to {@code AWAITING_PROCESSING} and notifies the buyer.
     *
     * @param vendorId the authenticated vendor's account UUID
     * @param orderId  the order UUID
     * @param locale   buyer notification locale
     * @return the updated order response
     */
    OrderResponse confirmWirePayment(UUID vendorId, UUID orderId, Locale locale);

    /**
     * Rejects a wire transfer payment: cancels the order, restores stock, and notifies the buyer.
     *
     * @param vendorId the authenticated vendor's account UUID
     * @param orderId  the order UUID
     * @param locale   buyer notification locale
     * @return the updated order response
     */
    OrderResponse rejectWirePayment(UUID vendorId, UUID orderId, Locale locale);

    /**
     * Marks an order as shipped by recording the carrier tracking number and transitioning the order
     * to {@code SHIPPED}. The buyer is notified by email (US-EXP-01).
     *
     * @param vendorId       the authenticated vendor's account UUID
     * @param orderId        the order UUID
     * @param trackingNumber the carrier-issued tracking number
     * @param locale         buyer notification locale
     * @return the updated order response
     */
    OrderResponse shipOrder(UUID vendorId, UUID orderId, String trackingNumber, Locale locale);
}
