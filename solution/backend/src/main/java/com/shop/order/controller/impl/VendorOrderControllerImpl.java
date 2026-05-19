package com.shop.order.controller.impl;

import com.shop.order.controller.VendorOrderController;
import com.shop.order.dto.OrderResponse;
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
}
