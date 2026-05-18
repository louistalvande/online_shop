package com.shop.order.service.impl;

import com.shop.account.repository.AccountRepository;
import com.shop.cart.entity.Cart;
import com.shop.cart.entity.CartItem;
import com.shop.cart.repository.CartRepository;
import com.shop.carrier.entity.Carrier;
import com.shop.carrier.repository.CarrierRepository;
import com.shop.catalog.entity.Product;
import com.shop.catalog.repository.ProductRepository;
import com.shop.common.repository.CountryRepository;
import com.shop.notification.service.NotificationService;
import com.shop.order.dto.CheckoutInitResponse;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderResponse;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderLine;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.exception.CarrierNotAvailableException;
import com.shop.order.exception.EmptyCartException;
import com.shop.order.exception.InvalidDeliveryCountryException;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.MissingBuyerIbanException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.exception.PaymentFailedException;
import com.shop.order.repository.OrderRepository;
import com.shop.payment.PaymentGateway;
import com.shop.payment.PaymentIntentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Manages the buyer order lifecycle (US-ORD-01..05). */
@Service
@Transactional
public class OrderServiceImpl implements com.shop.order.service.OrderService {

    private static final BigDecimal VAT_RATE = new BigDecimal("1.20");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CarrierRepository carrierRepository;
    private final CountryRepository countryRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;
    private final String bankIban;
    private final String bankBic;

    /**
     * Constructs the service with all required dependencies.
     *
     * @param orderRepository     JPA repository for orders
     * @param cartRepository      JPA repository for carts
     * @param carrierRepository   JPA repository for carriers
     * @param countryRepository   JPA repository for Eurozone country codes
     * @param accountRepository   JPA repository for accounts (vendor email lookup)
     * @param productRepository   JPA repository for products (stock restoration on cancellation)
     * @param paymentGateway      card payment abstraction (Stripe or stub)
     * @param notificationService email notification service
     * @param bankIban            vendor bank IBAN injected from configuration
     * @param bankBic             vendor bank BIC injected from configuration
     */
    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CarrierRepository carrierRepository,
            CountryRepository countryRepository,
            AccountRepository accountRepository,
            ProductRepository productRepository,
            PaymentGateway paymentGateway,
            NotificationService notificationService,
            @Value("${shop.bank.iban}") String bankIban,
            @Value("${shop.bank.bic}") String bankBic) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.carrierRepository = carrierRepository;
        this.countryRepository = countryRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.bankIban = bankIban;
        this.bankBic = bankBic;
    }

    /** {@inheritDoc} */
    @Override
    public CheckoutInitResponse initCheckout(UUID buyerId, CreateOrderRequest request, Locale locale) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(EmptyCartException::new);

        String countryCode = request.getDeliveryCountryCode();
        if (!countryRepository.existsByCode(countryCode)) {
            throw new InvalidDeliveryCountryException(countryCode);
        }

        Carrier carrier = carrierRepository.findById(request.getCarrierId())
                .filter(c -> c.isActive() && c.getSupportedCountries().contains(countryCode))
                .orElseThrow(() -> new CarrierNotAvailableException(request.getCarrierId()));

        UUID vendorId = resolveVendorId(cart);
        String vendorEmail = resolveVendorEmail(cart);

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setOrderNumber(generateOrderNumber());
        order.setCarrierId(carrier.getId());
        order.setCarrierName(carrier.getName());
        order.setCarrierTrackingUrl(carrier.getTrackingUrl());
        order.setDeliveryAddressLine(request.getDeliveryAddressLine());
        order.setDeliveryCity(request.getDeliveryCity());
        order.setDeliveryPostalCode(request.getDeliveryPostalCode());
        order.setDeliveryCountryCode(countryCode);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setVendorEmail(vendorEmail);
        order.setVendorId(vendorId);

        List<OrderLine> lines = buildLines(cart, order);
        order.getLines().addAll(lines);

        BigDecimal total = lines.stream()
                .map(OrderLine::getLineTotalTtc)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmountTtc(total);

        deductStock(cart);

        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            PaymentIntentResult intent = paymentGateway.createPaymentIntent(total, order.getId() != null ? order.getId() : UUID.randomUUID());
            order.setStripePaymentIntentId(intent.getPaymentIntentId());
            order.setStatus(OrderStatus.PAYMENT_PENDING_CARD);
            Order saved = orderRepository.save(order);
            cartRepository.delete(cart);
            return CheckoutInitResponse.forCard(saved.getId(), saved.getOrderNumber(),
                    saved.getTotalAmountTtc(), intent.getClientSecret());
        } else {
            order.setStatus(OrderStatus.PAYMENT_PENDING_WIRE);
            Order saved = orderRepository.save(order);
            cartRepository.delete(cart);
            String buyerEmail = accountRepository.findById(buyerId)
                    .map(a -> a.getEmail()).orElse("");
            notificationService.sendWireTransferDetailsEmail(
                    buyerEmail, saved.getOrderNumber(), saved.getTotalAmountTtc(),
                    bankIban, bankBic, locale);
            notificationService.sendVendorNewOrderEmail(
                    vendorEmail, OrderResponse.from(saved), locale);
            return CheckoutInitResponse.forWire(saved.getId(), saved.getOrderNumber(),
                    saved.getTotalAmountTtc(), bankIban, bankBic);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse confirmCardPayment(UUID buyerId, UUID orderId, Locale locale) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING_CARD) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        if (!paymentGateway.isPaymentSucceeded(order.getStripePaymentIntentId())) {
            throw new PaymentFailedException("Stripe reported payment not succeeded");
        }

        order.setStatus(OrderStatus.AWAITING_PROCESSING);
        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(buyerId)
                .map(a -> a.getEmail()).orElse("");
        notificationService.sendOrderConfirmationEmail(buyerEmail, response, locale);
        notificationService.sendVendorNewOrderEmail(saved.getVendorEmail(), response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(UUID buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(UUID buyerId, UUID orderId) {
        return orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Builds order line snapshots from the cart items and associates them with the given order.
     *
     * @param cart  the buyer's cart
     * @param order the new order being assembled
     * @return list of populated OrderLine entities (not yet persisted)
     */
    private List<OrderLine> buildLines(Cart cart, Order order) {
        return cart.getItems().stream().map(item -> {
            Product product = item.getProduct();
            BigDecimal unitPriceTtc = product.getPriceExclTax()
                    .multiply(VAT_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPriceTtc
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderLine line = new OrderLine();
            line.setOrder(order);
            line.setProductId(product.getId());
            line.setProductName(product.getName());
            line.setUnitPriceExclTax(product.getPriceExclTax());
            line.setUnitPriceTtc(unitPriceTtc);
            line.setQuantity(item.getQuantity());
            line.setLineTotalTtc(lineTotal);
            return line;
        }).toList();
    }

    /**
     * Deducts ordered quantities from product stock.
     *
     * @param cart the buyer's cart containing items and quantities
     */
    private void deductStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
        }
    }

    /**
     * Resolves the vendor UUID from the first cart item's product.
     *
     * @param cart the buyer's cart
     * @return the vendor account UUID, or null if not found
     */
    private UUID resolveVendorId(Cart cart) {
        return cart.getItems().stream()
                .findFirst()
                .map(item -> item.getProduct().getVendorId())
                .orElse(null);
    }

    /**
     * Resolves the vendor email by looking up the first cart item's product vendor.
     *
     * @param cart the buyer's cart
     * @return the vendor's email address, or empty string if not found
     */
    private String resolveVendorEmail(Cart cart) {
        return cart.getItems().stream()
                .findFirst()
                .map(item -> item.getProduct().getVendorId())
                .flatMap(accountRepository::findById)
                .map(a -> a.getEmail())
                .orElse("");
    }

    /** {@inheritDoc} */
    @Override
    public OrderResponse cancelOrder(UUID buyerId, UUID orderId, String buyerIban, Locale locale) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.AWAITING_PROCESSING
                && order.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        restoreOrderStock(order);

        if (order.getPaymentMethod() == PaymentMethod.WIRE_TRANSFER) {
            if (buyerIban == null || buyerIban.isBlank()) {
                throw new MissingBuyerIbanException(orderId);
            }
            order.setBuyerIban(buyerIban);
            order.setStatus(OrderStatus.WIRE_REFUND_IN_PROGRESS);
        } else {
            if (order.getStripePaymentIntentId() != null) {
                paymentGateway.refund(order.getStripePaymentIntentId());
            }
            order.setStatus(OrderStatus.CANCELLED);
        }

        Order saved = orderRepository.save(order);
        OrderResponse response = OrderResponse.from(saved);

        String buyerEmail = accountRepository.findById(buyerId).map(a -> a.getEmail()).orElse("");
        notificationService.sendBuyerCancellationEmail(buyerEmail, response, locale);
        notificationService.sendVendorCancellationEmail(saved.getVendorEmail(), response, locale);

        return response;
    }

    /**
     * Restores product stock for each order line (best-effort: skips deleted products).
     *
     * @param order the order whose stock should be restored
     */
    private void restoreOrderStock(Order order) {
        order.getLines().forEach(line -> {
            if (line.getProductId() != null) {
                productRepository.findById(line.getProductId()).ifPresent(product ->
                        product.setQuantity(product.getQuantity() + line.getQuantity()));
            }
        });
    }

    /**
     * Generates a human-readable order number in the format {@code ORD-YYYYMMDD-XXXXXXXX}.
     *
     * @return a unique order number
     */
    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DATE_FORMAT);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + suffix;
    }
}
