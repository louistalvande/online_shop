package com.shop.claim.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.repository.AccountRepository;
import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.dto.CreateClaimRequest;
import com.shop.claim.entity.Claim;
import com.shop.claim.entity.ClaimStatus;
import com.shop.claim.exception.ClaimAlreadyOpenException;
import com.shop.claim.exception.ClaimNotFoundException;
import com.shop.claim.exception.InvalidClaimStateException;
import com.shop.claim.repository.ClaimRepository;
import com.shop.claim.service.ClaimService;
import com.shop.notification.service.NotificationService;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Buyer-facing claim service implementation (US-CLM-01). */
@Service
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private static final Set<OrderStatus> CLAIMABLE_STATUSES = Set.of(
            OrderStatus.AWAITING_PROCESSING,
            OrderStatus.IN_PREPARATION,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );

    private final ClaimRepository claimRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;

    /**
     * @param claimRepository     JPA repository for claims
     * @param orderRepository     JPA repository for orders (ownership + status checks)
     * @param accountRepository   JPA repository for accounts (email resolution)
     * @param notificationService email notification service
     */
    public ClaimServiceImpl(
            ClaimRepository claimRepository,
            OrderRepository orderRepository,
            AccountRepository accountRepository,
            NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
    }

    /** {@inheritDoc} */
    @Override
    public ClaimResponse openClaim(String buyerEmail, UUID orderId, CreateClaimRequest request, Locale locale) {
        UUID buyerId = resolveAccountId(buyerEmail);

        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!CLAIMABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStateException(orderId, order.getStatus());
        }

        if (claimRepository.existsByOrderIdAndBuyerIdAndStatus(orderId, buyerId, ClaimStatus.OPEN)) {
            throw new ClaimAlreadyOpenException(orderId);
        }

        Claim claim = new Claim();
        claim.setOrderId(orderId);
        claim.setOrderNumber(order.getOrderNumber());
        claim.setBuyerId(buyerId);
        claim.setBuyerEmail(buyerEmail);
        claim.setVendorId(order.getVendorId());
        claim.setVendorEmail(order.getVendorEmail());
        claim.setReason(request.getReason());
        claim.setMessage(request.getMessage());
        claim.setStatus(ClaimStatus.OPEN);

        Claim saved = claimRepository.save(claim);
        ClaimResponse response = ClaimResponse.from(saved);

        notificationService.sendClaimOpenedEmail(order.getVendorEmail(), response, locale);

        return response;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getMyClaims(String buyerEmail) {
        UUID buyerId = resolveAccountId(buyerEmail);
        return claimRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(ClaimResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getMyClaim(String buyerEmail, UUID claimId) {
        UUID buyerId = resolveAccountId(buyerEmail);
        return claimRepository.findByIdAndBuyerId(claimId, buyerId)
                .map(ClaimResponse::from)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
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
