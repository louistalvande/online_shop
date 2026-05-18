package com.shop.order.service;

import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderResponse;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Business logic for buyer order lifecycle (US-ORD-01..05). */
public interface OrderService {

    /**
     * Creates an order from the buyer's current cart and initialises payment.
     * <ul>
     *   <li>CARD: creates a Stripe PaymentIntent and returns the client secret.</li>
     *   <li>WIRE: sets status to PAYMENT_PENDING_WIRE and emails bank details to the buyer.</li>
     * </ul>
     *
     * @param buyerId the authenticated buyer UUID
     * @param request delivery address and payment choice
     * @param locale  buyer locale for notification emails
     * @return checkout initialisation payload (client secret for card, bank details for wire)
     */
    CheckoutInitResponse initCheckout(UUID buyerId, CreateOrderRequest request, Locale locale);

    /**
     * Confirms that the Stripe card payment succeeded after the frontend called Stripe.js.
     * Transitions the order from PAYMENT_PENDING_CARD to AWAITING_PROCESSING
     * and sends confirmation emails (US-ORD-03, US-ORD-05).
     *
     * @param buyerId the authenticated buyer UUID
     * @param orderId the order UUID returned by {@link #initCheckout}
     * @param locale  buyer locale for notification emails
     * @return the updated order
     */
    OrderResponse confirmCardPayment(UUID buyerId, UUID orderId, Locale locale);

    /**
     * Returns all orders placed by the given buyer, newest first.
     *
     * @param buyerId the authenticated buyer UUID
     * @return list of order responses
     */
    List<OrderResponse> getMyOrders(UUID buyerId);

    /**
     * Returns a single order belonging to the given buyer.
     *
     * @param buyerId the authenticated buyer UUID
     * @param orderId the order UUID
     * @return the order response
     */
    OrderResponse getMyOrder(UUID buyerId, UUID orderId);

    /**
     * Cancels an order placed by the buyer (US-CAN-01).
     * Valid from {@code AWAITING_PROCESSING} or {@code IN_PREPARATION}.
     * <ul>
     *   <li>CARD payment: triggers Stripe refund, transitions to {@code CANCELLED}.</li>
     *   <li>WIRE payment: requires {@code buyerIban}; transitions to {@code WIRE_REFUND_IN_PROGRESS}.</li>
     * </ul>
     * Stock is always released and the vendor is notified.
     *
     * @param buyerId   the authenticated buyer UUID
     * @param orderId   the order UUID
     * @param buyerIban the buyer's IBAN for wire refund; {@code null} for card orders
     * @param locale    locale for notification emails
     * @return the updated order
     */
    OrderResponse cancelOrder(UUID buyerId, UUID orderId, String buyerIban, Locale locale);
}
