package com.shop.order.service.impl;

import com.shop.account.repository.AccountRepository;
import com.shop.catalog.repository.ProductRepository;
import com.shop.notification.service.NotificationService;
import com.shop.payment.PaymentGateway;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.MissingBuyerIbanException;
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
    private final PaymentGateway paymentGateway;

    /**
     * @param orderRepository     JPA repository for orders
     * @param productRepository   JPA repository for products (stock restoration)
     * @param accountRepository   JPA repository for accounts (buyer lookup)
     * @param notificationService email notification service
     * @param paymentGateway      card payment abstraction (Stripe refund)
     */
    public VendorOrderServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            AccountRepository accountRepository,
            NotificationService notificationService,
            PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getVendorOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getVendorOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse confirmWirePayment(UUID orderId, Locale locale) {
        Order order = orderRepository.findById(orderId)
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
    public OrderResponse rejectWirePayment(UUID orderId, Locale locale) {
        Order order = orderRepository.findById(orderId)
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

    /** {@inheritDoc} */
    @Override
    public OrderResponse shipOrder(UUID orderId, String trackingNumber, Locale locale) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.AWAITING_PROCESSING
                && order.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        order.setTrackingNumber(trackingNumber);
        order.setStatus(OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendShipmentNotificationEmail(buyerEmail, response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse acceptReturn(UUID orderId, String buyerIban, Locale locale) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.SHIPPED
                && order.getStatus() != OrderStatus.CANCELLATION_REQUESTED_AFTER_SHIPMENT) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        if (order.getPaymentMethod() == PaymentMethod.WIRE_TRANSFER) {
            if (buyerIban == null || buyerIban.isBlank()) {
                if (order.getBuyerIban() == null || order.getBuyerIban().isBlank()) {
                    throw new MissingBuyerIbanException(orderId);
                }
            } else {
                order.setBuyerIban(buyerIban);
            }
        }

        order.setStatus(OrderStatus.PENDING_RETURN);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendReturnRequestedEmail(buyerEmail, response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse confirmReturn(UUID orderId, Locale locale) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PENDING_RETURN) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        restoreStock(order);

        if (order.getPaymentMethod() == PaymentMethod.WIRE_TRANSFER) {
            order.setStatus(OrderStatus.WIRE_REFUND_IN_PROGRESS);
        } else {
            if (order.getStripePaymentIntentId() != null) {
                paymentGateway.refund(order.getStripePaymentIntentId());
            }
            order.setStatus(OrderStatus.CANCELLED);
        }

        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendBuyerCancellationEmail(buyerEmail, response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse waiveReturn(UUID orderId, String buyerIban, Locale locale) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.SHIPPED
                && order.getStatus() != OrderStatus.CANCELLATION_REQUESTED_AFTER_SHIPMENT) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        if (order.getPaymentMethod() == PaymentMethod.WIRE_TRANSFER) {
            if (buyerIban == null || buyerIban.isBlank()) {
                if (order.getBuyerIban() == null || order.getBuyerIban().isBlank()) {
                    throw new MissingBuyerIbanException(orderId);
                }
            } else {
                order.setBuyerIban(buyerIban);
            }
            order.setStatus(OrderStatus.WIRE_REFUND_IN_PROGRESS);
        } else {
            if (order.getStripePaymentIntentId() != null) {
                paymentGateway.refund(order.getStripePaymentIntentId());
            }
            order.setStatus(OrderStatus.CANCELLED);
        }

        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendBuyerCancellationEmail(buyerEmail, response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse confirmWireRefund(UUID orderId, Locale locale) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.WIRE_REFUND_IN_PROGRESS) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendWireRefundConfirmedEmail(buyerEmail, response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse refuseCancellationRequest(UUID orderId, Locale locale) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CANCELLATION_REQUESTED_AFTER_SHIPMENT) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(saved.getBuyerId())
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendCancellationRefusedEmail(buyerEmail, response, locale);

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
