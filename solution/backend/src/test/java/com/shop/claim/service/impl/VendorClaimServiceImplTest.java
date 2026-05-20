package com.shop.claim.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.entity.Claim;
import com.shop.claim.entity.ClaimDecision;
import com.shop.claim.entity.ClaimReason;
import com.shop.claim.entity.ClaimStatus;
import com.shop.claim.exception.ClaimNotFoundException;
import com.shop.claim.exception.InvalidClaimStateException;
import com.shop.claim.repository.ClaimRepository;
import com.shop.notification.service.NotificationService;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.repository.OrderRepository;
import com.shop.payment.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link VendorClaimServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class VendorClaimServiceImplTest {

    @Mock ClaimRepository claimRepository;
    @Mock OrderRepository orderRepository;
    @Mock AccountRepository accountRepository;
    @Mock NotificationService notificationService;
    @Mock PaymentGateway paymentGateway;

    VendorClaimServiceImpl service;

    private static final UUID VENDOR_ID = UUID.randomUUID();
    private static final UUID BUYER_ID  = UUID.randomUUID();
    private static final UUID ORDER_ID  = UUID.randomUUID();
    private static final UUID CLAIM_ID  = UUID.randomUUID();
    private static final String VENDOR_EMAIL = "vendor@test.com";
    private static final String BUYER_EMAIL  = "buyer@test.com";

    @BeforeEach
    void setUp() {
        service = new VendorClaimServiceImpl(
                claimRepository, orderRepository, accountRepository, notificationService, paymentGateway);

        Account vendor = new Account();
        setField(vendor, "id", VENDOR_ID);
        vendor.setEmail(VENDOR_EMAIL);
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendor));
    }

    // ─── getVendorClaims ─────────────────────────────────────────────────────

    @Test
    void getVendorClaims_returnsAllClaims() {
        given(claimRepository.findByVendorIdOrderByCreatedAtDesc(VENDOR_ID))
                .willReturn(List.of(buildClaim(ClaimStatus.OPEN)));

        List<ClaimResponse> result = service.getVendorClaims(VENDOR_EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ClaimStatus.OPEN);
    }

    // ─── getVendorClaim ──────────────────────────────────────────────────────

    @Test
    void getVendorClaim_notFound_throws() {
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVendorClaim(VENDOR_EMAIL, CLAIM_ID))
                .isInstanceOf(ClaimNotFoundException.class);
    }

    // ─── grantRefund ─────────────────────────────────────────────────────────

    @Test
    void grantRefund_card_triggersStripeRefundAndClosesClaim() {
        Claim claim = buildClaim(ClaimStatus.OPEN);
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.of(claim));
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(buildOrder(PaymentMethod.CARD, "pi_stub")));
        given(claimRepository.save(any())).willReturn(claim);

        ClaimResponse result = service.grantRefund(VENDOR_EMAIL, CLAIM_ID, Locale.FRENCH);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.CLOSED);
        assertThat(result.getDecision()).isEqualTo(ClaimDecision.GRANTED);
        then(paymentGateway).should().refund("pi_stub");
        then(notificationService).should().sendClaimGrantedEmail(any(), any(), any());
    }

    @Test
    void grantRefund_wire_closesClaimWithoutStripe() {
        Claim claim = buildClaim(ClaimStatus.OPEN);
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.of(claim));
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(buildOrder(PaymentMethod.WIRE_TRANSFER, null)));
        given(claimRepository.save(any())).willReturn(claim);

        service.grantRefund(VENDOR_EMAIL, CLAIM_ID, Locale.FRENCH);

        then(paymentGateway).shouldHaveNoInteractions();
        then(notificationService).should().sendClaimGrantedEmail(any(), any(), any());
    }

    @Test
    void grantRefund_claimAlreadyClosed_throws() {
        Claim claim = buildClaim(ClaimStatus.CLOSED);
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.of(claim));

        assertThatThrownBy(() -> service.grantRefund(VENDOR_EMAIL, CLAIM_ID, Locale.FRENCH))
                .isInstanceOf(InvalidClaimStateException.class);
    }

    @Test
    void grantRefund_notFound_throws() {
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.grantRefund(VENDOR_EMAIL, CLAIM_ID, Locale.FRENCH))
                .isInstanceOf(ClaimNotFoundException.class);
    }

    // ─── refuseRefund ────────────────────────────────────────────────────────

    @Test
    void refuseRefund_closesClaimWithRefusedDecision() {
        Claim claim = buildClaim(ClaimStatus.OPEN);
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.of(claim));
        given(claimRepository.save(any())).willReturn(claim);

        ClaimResponse result = service.refuseRefund(VENDOR_EMAIL, CLAIM_ID, Locale.FRENCH);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.CLOSED);
        assertThat(result.getDecision()).isEqualTo(ClaimDecision.REFUSED);
        then(paymentGateway).shouldHaveNoInteractions();
        then(notificationService).should().sendClaimRefusedEmail(any(), any(), any());
    }

    @Test
    void refuseRefund_claimAlreadyClosed_throws() {
        Claim claim = buildClaim(ClaimStatus.CLOSED);
        given(claimRepository.findByIdAndVendorId(CLAIM_ID, VENDOR_ID)).willReturn(Optional.of(claim));

        assertThatThrownBy(() -> service.refuseRefund(VENDOR_EMAIL, CLAIM_ID, Locale.FRENCH))
                .isInstanceOf(InvalidClaimStateException.class);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Claim buildClaim(ClaimStatus status) {
        Claim c = new Claim();
        setField(c, "id", CLAIM_ID);
        c.setOrderId(ORDER_ID);
        c.setOrderNumber("ORD-TEST-001");
        c.setBuyerId(BUYER_ID);
        c.setBuyerEmail(BUYER_EMAIL);
        c.setVendorId(VENDOR_ID);
        c.setVendorEmail(VENDOR_EMAIL);
        c.setReason(ClaimReason.DEFECTIVE_ITEM);
        c.setMessage("Item arrived broken.");
        c.setStatus(status);
        return c;
    }

    private Order buildOrder(PaymentMethod paymentMethod, String stripeId) {
        Order o = new Order();
        setField(o, "id", ORDER_ID);
        setField(o, "lines", new ArrayList<>());
        o.setBuyerId(BUYER_ID);
        o.setVendorId(VENDOR_ID);
        o.setVendorEmail(VENDOR_EMAIL);
        o.setOrderNumber("ORD-TEST-001");
        o.setCarrierId(UUID.randomUUID());
        o.setCarrierName("Test Carrier");
        o.setCarrierTrackingUrl("https://track.example.com");
        o.setDeliveryAddressLine("1 rue Test");
        o.setDeliveryCity("Paris");
        o.setDeliveryPostalCode("75001");
        o.setDeliveryCountryCode("FR");
        o.setPaymentMethod(paymentMethod);
        o.setStatus(OrderStatus.SHIPPED);
        o.setTotalAmountTtc(new BigDecimal("24.00"));
        o.setStripePaymentIntentId(stripeId);
        return o;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            var f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
