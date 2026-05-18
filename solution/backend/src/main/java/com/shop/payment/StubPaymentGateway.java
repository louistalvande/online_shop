package com.shop.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * No-op payment gateway used when {@code stripe.secret-key} is not configured (dev/test).
 * Always reports payment as succeeded so the checkout flow can be exercised without real Stripe credentials.
 */
@Component
@ConditionalOnMissingBean(StripePaymentGateway.class)
public class StubPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StubPaymentGateway.class);

    /** {@inheritDoc} — returns a dummy client secret; no real Stripe call is made. */
    @Override
    public PaymentIntentResult createPaymentIntent(BigDecimal amountTtc, UUID orderId) {
        String fakeId = "pi_stub_" + orderId;
        log.info("[PAYMENT-STUB] Created fake PaymentIntent {} for order {} ({}€)", fakeId, orderId, amountTtc);
        return new PaymentIntentResult(fakeId, fakeId + "_secret");
    }

    /** {@inheritDoc} — always returns {@code true} in stub mode. */
    @Override
    public boolean isPaymentSucceeded(String paymentIntentId) {
        log.info("[PAYMENT-STUB] Payment succeeded check for {} → true", paymentIntentId);
        return true;
    }

    /** {@inheritDoc} — no-op in stub mode. */
    @Override
    public void refund(String paymentIntentId) {
        log.info("[PAYMENT-STUB] Refund triggered for {} (no-op)", paymentIntentId);
    }
}
