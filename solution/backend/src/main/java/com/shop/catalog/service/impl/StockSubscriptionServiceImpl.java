package com.shop.catalog.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.catalog.dto.StockSubscriptionResponse;
import com.shop.catalog.entity.BackInStockSubscription;
import com.shop.catalog.entity.Product;
import com.shop.catalog.exception.AlreadySubscribedException;
import com.shop.catalog.exception.ProductInStockException;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.exception.StockSubscriptionNotFoundException;
import com.shop.catalog.repository.BackInStockSubscriptionRepository;
import com.shop.catalog.repository.ProductRepository;
import com.shop.catalog.service.StockSubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** {@link StockSubscriptionService} implementation. */
@Service
@Transactional
public class StockSubscriptionServiceImpl implements StockSubscriptionService {

    private final BackInStockSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    /**
     * Constructs the service with its required repositories.
     *
     * @param subscriptionRepository repository for stock subscriptions
     * @param productRepository      repository for products
     * @param accountRepository      repository for accounts
     */
    public StockSubscriptionServiceImpl(
            BackInStockSubscriptionRepository subscriptionRepository,
            ProductRepository productRepository,
            AccountRepository accountRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
    }

    /** {@inheritDoc} */
    @Override
    public StockSubscriptionResponse subscribe(String buyerEmail, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getQuantity() > 0) {
            throw new ProductInStockException(productId);
        }

        Account buyer = accountRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found: " + buyerEmail));

        if (subscriptionRepository.existsByBuyer_IdAndProduct_Id(buyer.getId(), productId)) {
            throw new AlreadySubscribedException(productId);
        }

        BackInStockSubscription sub = new BackInStockSubscription();
        sub.setBuyer(buyer);
        sub.setProduct(product);

        return StockSubscriptionResponse.from(subscriptionRepository.save(sub));
    }

    /** {@inheritDoc} */
    @Override
    public void unsubscribe(String buyerEmail, UUID productId) {
        Account buyer = accountRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found: " + buyerEmail));

        BackInStockSubscription sub = subscriptionRepository
                .findByBuyer_IdAndProduct_Id(buyer.getId(), productId)
                .orElseThrow(() -> new StockSubscriptionNotFoundException(productId));

        subscriptionRepository.delete(sub);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<StockSubscriptionResponse> listSubscriptions(String buyerEmail) {
        Account buyer = accountRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found: " + buyerEmail));

        return subscriptionRepository
                .findByBuyer_IdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(StockSubscriptionResponse::from)
                .toList();
    }
}
