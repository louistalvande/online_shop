package com.shop.order.controller;

import com.shop.order.dto.CancelOrderRequest;
import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderResponse;
import com.shop.order.dto.RequestPostShipmentCancellationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** REST API for buyer order lifecycle (US-ORD-01..05). */
@Tag(name = "Orders", description = "Buyer order lifecycle")
@RequestMapping("/api/orders")
public interface OrderController {

    /**
     * Creates an order from the buyer's current cart and initiates payment.
     *
     * @param request   delivery address and payment method
     * @param principal authenticated buyer
     * @param locale    buyer locale (from Accept-Language header)
     * @return 201 with checkout initialisation payload
     */
    @Operation(summary = "Create order from cart and initiate payment (US-ORD-01..04)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or empty cart"),
            @ApiResponse(responseCode = "409", description = "Carrier not available for the delivery country"),
            @ApiResponse(responseCode = "422", description = "Delivery country not in Eurozone")
    })
    @PostMapping
    ResponseEntity<CheckoutInitResponse> initCheckout(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal,
            Locale locale);

    /**
     * Confirms that the Stripe card payment succeeded (called after Stripe.js completes).
     *
     * @param orderId   the order UUID returned by {@link #initCheckout}
     * @param principal authenticated buyer
     * @param locale    buyer locale
     * @return 200 with the updated order
     */
    @Operation(summary = "Confirm card payment after Stripe.js confirmation (US-ORD-03)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment confirmed, order awaiting processing"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order not in PAYMENT_PENDING_CARD status"),
            @ApiResponse(responseCode = "402", description = "Payment not succeeded")
    })
    @PostMapping("/{orderId}/confirm-payment")
    ResponseEntity<OrderResponse> confirmCardPayment(
            @PathVariable UUID orderId,
            Principal principal,
            Locale locale);

    /**
     * Returns all orders for the authenticated buyer.
     *
     * @param principal authenticated buyer
     * @return 200 with list of orders
     */
    @Operation(summary = "List all orders for the authenticated buyer")
    @ApiResponse(responseCode = "200", description = "Orders retrieved")
    @GetMapping
    ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal);

    /**
     * Returns a single order for the authenticated buyer.
     *
     * @param orderId   the order UUID
     * @param principal authenticated buyer
     * @return 200 with the order
     */
    @Operation(summary = "Get a specific order for the authenticated buyer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> getMyOrder(
            @PathVariable UUID orderId,
            Principal principal);

    /**
     * Cancels an order placed by the authenticated buyer (US-CAN-01).
     * Valid when status is AWAITING_PROCESSING only (not once in preparation).
     * Wire orders require a buyerIban for refund.
     *
     * @param orderId   the order UUID
     * @param request   optional IBAN for wire refund
     * @param principal authenticated buyer
     * @param locale    buyer locale
     * @return 200 with the updated order
     */
    @Operation(summary = "Cancel an order placed by the buyer (US-CAN-01)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled or wire refund initiated"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order not in a cancellable status"),
            @ApiResponse(responseCode = "422", description = "Buyer IBAN missing for wire transfer order")
    })
    @PostMapping("/{orderId}/cancel")
    ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request,
            Principal principal,
            Locale locale);

    /**
     * Records the buyer's post-shipment cancellation request (US-CAN-06).
     * Valid when the order status is {@code SHIPPED}.
     * Wire transfer orders require a buyerIban in the request body.
     *
     * @param orderId   the order UUID
     * @param request   cancellation reason and optional IBAN
     * @param principal authenticated buyer
     * @param locale    buyer locale
     * @return 200 with the updated order
     */
    @Operation(summary = "Request post-shipment cancellation (US-CAN-06)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancellation request recorded, vendor notified"),
            @ApiResponse(responseCode = "400", description = "Reason missing or request invalid"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order not in SHIPPED status"),
            @ApiResponse(responseCode = "422", description = "Buyer IBAN missing for wire transfer order")
    })
    @PostMapping("/{orderId}/request-post-shipment-cancellation")
    ResponseEntity<OrderResponse> requestPostShipmentCancellation(
            @PathVariable UUID orderId,
            @Valid @RequestBody RequestPostShipmentCancellationRequest request,
            Principal principal,
            Locale locale);
}
