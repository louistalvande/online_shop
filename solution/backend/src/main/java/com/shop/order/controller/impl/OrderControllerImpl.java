package com.shop.order.controller.impl;

import com.shop.order.controller.OrderController;
import com.shop.order.dto.CancelOrderRequest;
import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.OrderResponse;
import com.shop.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** REST controller for buyer order lifecycle. */
@RestController
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;

    /**
     * Constructs the controller with the order service.
     *
     * @param orderService the order business logic
     */
    public OrderControllerImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CheckoutInitResponse> initCheckout(
            com.shop.order.dto.CreateOrderRequest request,
            Principal principal,
            Locale locale) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.initCheckout(principal.getName(), request, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> confirmCardPayment(
            UUID orderId,
            Principal principal,
            Locale locale) {
        return ResponseEntity.ok(orderService.confirmCardPayment(principal.getName(), orderId, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> getMyOrder(UUID orderId, Principal principal) {
        return ResponseEntity.ok(orderService.getMyOrder(principal.getName(), orderId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<OrderResponse> cancelOrder(
            UUID orderId,
            CancelOrderRequest request,
            Principal principal,
            Locale locale) {
        String buyerIban = request != null ? request.getBuyerIban() : null;
        return ResponseEntity.ok(orderService.cancelOrder(principal.getName(), orderId, buyerIban, locale));
    }
}
