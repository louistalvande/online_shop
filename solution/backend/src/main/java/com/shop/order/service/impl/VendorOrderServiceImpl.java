package com.shop.order.service.impl;

import com.shop.account.repository.AccountRepository;
import com.shop.catalog.repository.ProductRepository;
import com.shop.notification.service.NotificationService;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.repository.OrderRepository;
import com.shop.order.service.VendorOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-side order management: listing, wire confirmation, and wire rejection (US-VND-01..02). */
@Service
@Transactional
public class VendorOrderServiceImpl implements VendorOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;

    /**
     * @param orderRepository    JPA repository for orders
     * @param productRepository  JPA repository for products (stock restoration)
     * @param accountRepository  JPA repository for accounts (buyer email lookup)
     * @param notificationService email notification service
     */
    public VendorOrderServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            AccountRepository accountRepository,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getVendorOrders(UUID vendorId) {
        return orderRepository.findByVendorIdOrderByCreatedAtDesc(vendorId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getVendorOrder(UUID vendorId, UUID orderId) {
        return orderRepository.findByIdAndVendorId(orderId, vendorId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse confirmWirePayment(UUID vendorId, UUID orderId, Locale locale) {
        Order order = orderRepository.findByIdAndVendorId(orderId, vendorId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING_WIRE) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        order.setStatus(OrderStatus.AWAITING_PROCESSING);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendOrderConfirmationEmail(buyerEmail, response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse rejectWirePayment(UUID vendorId, UUID orderId, Locale locale) {
        Order order = orderRepository.findByIdAndVendorId(orderId, vendorId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING_WIRE) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendWirePaymentRejectedEmail(buyerEmail, response, locale);

        return response;
    }

    /**
     * Restores product stock for each order line (best-effort: skips deleted products).
     *
     * @param order the order whose stock should be restored
     */
    private void restoreStock(Order order) {
        order.getLines().forEach(line -> {
            if (line.getProductId() != null) {
                productRepository.findById(line.getProductId()).ifPresent(product ->
                        product.setQuantity(product.getQuantity() + line.getQuantity()));
            }
        });
    }
}
