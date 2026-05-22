package com.shop.order.controller;

import com.shop.order.dto.OrderResponse;
import com.shop.order.dto.ShipOrderRequest;
import com.shop.order.dto.VendorReturnRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-side order endpoints: listing, wire confirmation, and rejection (US-VND-01..02). */
@Tag(name = "Vendor Orders", description = "Vendor order management")
@RequestMapping("/api/vendor/orders")
public interface VendorOrderController {

    /**
     * Lists all orders belonging to the authenticated vendor.
     *
     * @param principal the authenticated vendor principal
     * @return 200 with the list of orders
     */
    @Operation(summary = "List vendor orders")
    @ApiResponse(responseCode = "200", description = "Orders listed")
    @GetMapping
    ResponseEntity<List<OrderResponse>> listOrders(Principal principal);

    /**
     * Returns a single order belonging to the authenticated vendor.
     *
     * @param orderId   the order UUID
     * @param principal the authenticated vendor principal
     * @return 200 with the order, or 404 if not found / not owned
     */
    @Operation(summary = "Get vendor order details")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId, Principal principal);

    /**
     * Confirms receipt of a wire transfer, transitioning the order to AWAITING_PROCESSING.
     *
     * @param orderId   the order UUID
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Confirm wire transfer receipt")
    @ApiResponse(responseCode = "200", description = "Wire confirmed, order awaiting processing")
    @ApiResponse(responseCode = "409", description = "Order not in PAYMENT_PENDING_WIRE state")
    @PostMapping("/{orderId}/confirm-wire")
    ResponseEntity<OrderResponse> confirmWire(@PathVariable UUID orderId, Principal principal, Locale locale);

    /**
     * Rejects a wire transfer payment, cancelling the order and restoring stock.
     *
     * @param orderId   the order UUID
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the cancelled order
     */
    @Operation(summary = "Reject wire transfer payment")
    @ApiResponse(responseCode = "200", description = "Wire rejected, order cancelled")
    @ApiResponse(responseCode = "409", description = "Order not in PAYMENT_PENDING_WIRE state")
    @PostMapping("/{orderId}/reject-wire")
    ResponseEntity<OrderResponse> rejectWire(@PathVariable UUID orderId, Principal principal, Locale locale);

    /**
     * Declares shipment of an order by recording the carrier tracking number.
     * Transitions the order to {@code SHIPPED} and notifies the buyer (US-EXP-01).
     *
     * @param orderId   the order UUID
     * @param request   the shipment request containing the tracking number
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Declare order shipment with tracking number")
    @ApiResponse(responseCode = "200", description = "Order marked as shipped")
    @ApiResponse(responseCode = "409", description = "Order not in a shippable state")
    @PostMapping("/{orderId}/ship")
    ResponseEntity<OrderResponse> ship(@PathVariable UUID orderId,
                                       @Valid @RequestBody ShipOrderRequest request,
                                       Principal principal, Locale locale);

    /**
     * Accepts a post-shipment cancellation requiring parcel return (US-CAN-03).
     * Transitions the order from {@code SHIPPED} to {@code PENDING_RETURN} and notifies the buyer.
     *
     * @param orderId   the order UUID
     * @param request   optional buyer IBAN for wire refund
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Accept post-shipment cancellation with return requirement (US-CAN-03)")
    @ApiResponse(responseCode = "200", description = "Order moved to PENDING_RETURN")
    @ApiResponse(responseCode = "409", description = "Order not in SHIPPED state")
    @ApiResponse(responseCode = "422", description = "Buyer IBAN missing for wire transfer order")
    @PostMapping("/{orderId}/accept-return")
    ResponseEntity<OrderResponse> acceptReturn(@PathVariable UUID orderId,
                                               @RequestBody(required = false) VendorReturnRequest request,
                                               Principal principal, Locale locale);

    /**
     * Confirms receipt of the returned parcel and triggers refund (US-CAN-03).
     * Transitions from {@code PENDING_RETURN} to {@code CANCELLED} (card) or {@code WIRE_REFUND_IN_PROGRESS} (wire).
     *
     * @param orderId   the order UUID
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Confirm return parcel received and process refund (US-CAN-03)")
    @ApiResponse(responseCode = "200", description = "Return confirmed, refund triggered")
    @ApiResponse(responseCode = "409", description = "Order not in PENDING_RETURN state")
    @PostMapping("/{orderId}/confirm-return")
    ResponseEntity<OrderResponse> confirmReturn(@PathVariable UUID orderId,
                                                Principal principal, Locale locale);

    /**
     * Accepts a post-shipment cancellation without requiring return (US-CAN-04).
     * Transitions from {@code SHIPPED} to {@code CANCELLED} (card) or {@code WIRE_REFUND_IN_PROGRESS} (wire).
     *
     * @param orderId   the order UUID
     * @param request   optional buyer IBAN for wire refund
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Accept post-shipment cancellation without return requirement (US-CAN-04)")
    @ApiResponse(responseCode = "200", description = "Cancellation accepted, refund triggered or wire initiated")
    @ApiResponse(responseCode = "409", description = "Order not in SHIPPED state")
    @ApiResponse(responseCode = "422", description = "Buyer IBAN missing for wire transfer order")
    @PostMapping("/{orderId}/waive-return")
    ResponseEntity<OrderResponse> waiveReturn(@PathVariable UUID orderId,
                                              @RequestBody(required = false) VendorReturnRequest request,
                                              Principal principal, Locale locale);

    /**
     * Confirms that the wire transfer refund has been sent to the buyer (US-CAN-05).
     * Transitions from {@code WIRE_REFUND_IN_PROGRESS} to {@code CANCELLED}.
     *
     * @param orderId   the order UUID
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Confirm wire refund has been sent to the buyer (US-CAN-05)")
    @ApiResponse(responseCode = "200", description = "Wire refund confirmed, order cancelled")
    @ApiResponse(responseCode = "409", description = "Order not in WIRE_REFUND_IN_PROGRESS state")
    @PostMapping("/{orderId}/confirm-wire-refund")
    ResponseEntity<OrderResponse> confirmWireRefund(@PathVariable UUID orderId,
                                                    Principal principal, Locale locale);

    /**
     * Refuses the buyer's post-shipment cancellation request (US-CAN-06).
     * Transitions the order from {@code CANCELLATION_REQUESTED_AFTER_SHIPMENT} back to {@code SHIPPED}
     * and notifies the buyer of the refusal.
     *
     * @param orderId   the order UUID
     * @param principal the authenticated vendor principal
     * @param locale    locale for buyer notification
     * @return 200 with the updated order
     */
    @Operation(summary = "Refuse buyer's post-shipment cancellation request (US-CAN-06)")
    @ApiResponse(responseCode = "200", description = "Cancellation refused, order back to SHIPPED")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order not in CANCELLATION_REQUESTED_AFTER_SHIPMENT state")
    @PostMapping("/{orderId}/refuse-cancellation")
    ResponseEntity<OrderResponse> refuseCancellation(@PathVariable UUID orderId,
                                                     Principal principal, Locale locale);
}
