package com.shop.claim.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.repository.AccountRepository;
import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.entity.Claim;
import com.shop.claim.entity.ClaimDecision;
import com.shop.claim.entity.ClaimStatus;
import com.shop.claim.exception.ClaimNotFoundException;
import com.shop.claim.exception.InvalidClaimStateException;
import com.shop.claim.repository.ClaimRepository;
import com.shop.claim.service.VendorClaimService;
import com.shop.notification.service.NotificationService;
import com.shop.order.entity.Order;
import com.shop.order.entity.PaymentMethod;
import com.shop.order.repository.OrderRepository;
import com.shop.payment.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-facing claim management service implementation (US-CLM-02). */
@Service
@Transactional
public class VendorClaimServiceImpl implements VendorClaimService {

    private final ClaimRepository claimRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;

    /**
     * @param claimRepository     JPA repository for claims
     * @param orderRepository     JPA repository for orders (payment method + refund)
     * @param accountRepository   JPA repository for accounts (email resolution)
     * @param notificationService email notification service
     * @param paymentGateway      card payment abstraction (Stripe refund)
     */
    public VendorClaimServiceImpl(
            ClaimRepository claimRepository,
            OrderRepository orderRepository,
            AccountRepository accountRepository,
            NotificationService notificationService,
            PaymentGateway paymentGateway) {
        this.claimRepository = claimRepository;
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getVendorClaims(String vendorEmail) {
        UUID vendorId = resolveAccountId(vendorEmail);
        return claimRepository.findByVendorIdOrderByCreatedAtDesc(vendorId)
                .stream()
                .map(ClaimResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getVendorClaim(String vendorEmail, UUID claimId) {
        UUID vendorId = resolveAccountId(vendorEmail);
        return claimRepository.findByIdAndVendorId(claimId, vendorId)
                .map(ClaimResponse::from)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
    }

    /** {@inheritDoc} */
    @Override
    public ClaimResponse grantRefund(String vendorEmail, UUID claimId, Locale locale) {
        UUID vendorId = resolveAccountId(vendorEmail);
        Claim claim = claimRepository.findByIdAndVendorId(claimId, vendorId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        if (claim.getStatus() != ClaimStatus.OPEN) {
            throw new InvalidClaimStateException(claimId, claim.getStatus());
        }

        orderRepository.findById(claim.getOrderId()).ifPresent(order -> {
            if (order.getPaymentMethod() == PaymentMethod.CARD
                    && order.getStripePaymentIntentId() != null) {
                paymentGateway.refund(order.getStripePaymentIntentId());
            }
        });

        claim.setDecision(ClaimDecision.GRANTED);
        claim.setStatus(ClaimStatus.CLOSED);
        Claim saved = claimRepository.save(claim);
        ClaimResponse response = ClaimResponse.from(saved);

        notificationService.sendClaimGrantedEmail(claim.getBuyerEmail(), response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public ClaimResponse refuseRefund(String vendorEmail, UUID claimId, Locale locale) {
        UUID vendorId = resolveAccountId(vendorEmail);
        Claim claim = claimRepository.findByIdAndVendorId(claimId, vendorId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        if (claim.getStatus() != ClaimStatus.OPEN) {
            throw new InvalidClaimStateException(claimId, claim.getStatus());
        }

        claim.setDecision(ClaimDecision.REFUSED);
        claim.setStatus(ClaimStatus.CLOSED);
        Claim saved = claimRepository.save(claim);
        ClaimResponse response = ClaimResponse.from(saved);

        notificationService.sendClaimRefusedEmail(claim.getBuyerEmail(), response, locale);

        return response;
    }

    /**
     * Resolves the account UUID for the given email address.
     *
     * @param email the account email
     * @return the account UUID
     * @throws AccountNotFoundException if no account exists with that email
     */
    private UUID resolveAccountId(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getId)
                .orElseThrow(() -> new AccountNotFoundException(email));
    }
}
