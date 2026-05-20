package com.shop.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.shop.order.exception.PaymentFailedException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stripe implementation of {@link PaymentGateway}.
 * Active only when {@code stripe.secret-key} is configured.
 * Card data is collected exclusively by Stripe.js on the frontend — this backend
 * never sees raw card numbers (PCI-DSS, IFS-04).
 */
@Component
@ConditionalOnExpression("!'${stripe.secret-key:}'.isEmpty()")
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final String secretKey;

    /**
     * Constructs the gateway and sets the Stripe API key.
     *
     * @param secretKey Stripe secret key injected from {@code stripe.secret-key}
     */
    public StripePaymentGateway(@Value("${stripe.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    /** Registers the Stripe API key once the bean is initialised. */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /** {@inheritDoc} */
    @Override
    public PaymentIntentResult createPaymentIntent(BigDecimal amountTtc, UUID orderId) {
        long amountCents = amountTtc.multiply(BigDecimal.valueOf(100)).longValue();
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("eur")
                .putMetadata("orderId", orderId.toString())
                .build();
        try {
            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentIntentResult(intent.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            log.warn("[STRIPE] Failed to create PaymentIntent for order {}: {}", orderId, e.getMessage());
            throw new PaymentFailedException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPaymentSucceeded(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(intent.getStatus());
        } catch (StripeException e) {
            log.warn("[STRIPE] Failed to retrieve PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void refund(String paymentIntentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            Refund.create(params);
        } catch (StripeException e) {
            log.warn("[STRIPE] Failed to refund PaymentIntent {}: {}", paymentIntentId, e.getMessage());
            throw new PaymentFailedException(e.getMessage());
        }
    }
}
