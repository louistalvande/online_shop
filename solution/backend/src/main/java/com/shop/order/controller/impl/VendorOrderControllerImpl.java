package com.shop.order.controller.impl;

import com.shop.order.controller.VendorOrderController;
import com.shop.order.dto.OrderResponse;
import com.shop.order.dto.ShipOrderRequest;
import com.shop.order.dto.VendorReturnRequest;
import com.shop.order.service.VendorOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** REST implementation of {@link VendorOrderController}. */
@RestController
public class VendorOrderControllerImpl implements VendorOrderController {

    private final VendorOrderService vendorOrderService;

    /**
     * @param vendorOrderService the vendor order service
     */
    public VendorOrderControllerImpl(VendorOrderService vendorOrderService) {
        this.vendorOrderService = vendorOrderService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<OrderResponse>> listOrders(Principal principal) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.getVendorOrders(vendorId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID orderId, Principal principal) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.getVendorOrder(vendorId, orderId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> confirmWire(UUID orderId, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.confirmWirePayment(vendorId, orderId, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> rejectWire(UUID orderId, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.rejectWirePayment(vendorId, orderId, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> ship(UUID orderId, ShipOrderRequest request, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.shipOrder(vendorId, orderId, request.getTrackingNumber(), locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> acceptReturn(UUID orderId, VendorReturnRequest request, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        String buyerIban = request != null ? request.getBuyerIban() : null;
        return ResponseEntity.ok(vendorOrderService.acceptReturn(vendorId, orderId, buyerIban, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> confirmReturn(UUID orderId, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.confirmReturn(vendorId, orderId, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> waiveReturn(UUID orderId, VendorReturnRequest request, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        String buyerIban = request != null ? request.getBuyerIban() : null;
        return ResponseEntity.ok(vendorOrderService.waiveReturn(vendorId, orderId, buyerIban, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> confirmWireRefund(UUID orderId, Principal principal, Locale locale) {
        UUID vendorId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(vendorOrderService.confirmWireRefund(vendorId, orderId, locale));
    }
}
