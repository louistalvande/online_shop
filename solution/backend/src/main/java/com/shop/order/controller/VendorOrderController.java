package com.shop.order.controller;

import com.shop.order.dto.OrderResponse;
import com.shop.order.dto.ShipOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                                       @RequestBody ShipOrderRequest request,
                                       Principal principal, Locale locale);
}
