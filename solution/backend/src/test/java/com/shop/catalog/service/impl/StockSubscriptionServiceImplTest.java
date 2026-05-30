package com.shop.catalog.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.repository.AccountRepository;
import com.shop.catalog.dto.StockSubscriptionResponse;
import com.shop.catalog.entity.BackInStockSubscription;
import com.shop.catalog.entity.Product;
import com.shop.catalog.entity.ProductStatus;
import com.shop.catalog.exception.AlreadySubscribedException;
import com.shop.catalog.exception.ProductInStockException;
import com.shop.catalog.exception.ProductNotFoundException;
import com.shop.catalog.exception.StockSubscriptionNotFoundException;
import com.shop.catalog.repository.BackInStockSubscriptionRepository;
import com.shop.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/** Unit tests for {@link StockSubscriptionServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class StockSubscriptionServiceImplTest {

    @Mock BackInStockSubscriptionRepository subscriptionRepository;
    @Mock ProductRepository productRepository;
    @Mock AccountRepository accountRepository;

    StockSubscriptionServiceImpl service;

    private static final String BUYER_EMAIL = "buyer@example.com";
    private static final UUID   BUYER_ID    = UUID.randomUUID();
    private static final UUID   PRODUCT_ID  = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new StockSubscriptionServiceImpl(
                subscriptionRepository, productRepository, accountRepository);
    }

    private Account buyer() {
        Account a = new Account();
        a.setEmail(BUYER_EMAIL);
        a.setFirstName("Alice");
        a.setLastName("Buyer");
        a.setRole(AccountRole.BUYER);
        a.setStatus(AccountStatus.ACTIVE);
        a.setLanguage(AccountLanguage.FR);
        return a;
    }

    private Product outOfStockProduct() {
        Product p = new Product();
        p.setName("Aquarelle test");
        p.setPriceExclTax(new BigDecimal("25.00"));
        p.setQuantity(0);
        p.setStatus(ProductStatus.PUBLISHED);
        return p;
    }

    private Product inStockProduct() {
        Product p = outOfStockProduct();
        p.setQuantity(5);
        return p;
    }

    /** subscribe must create and return a subscription when product is out of stock and not yet subscribed. */
    @Test
    void subscribe_createsSubscription_whenProductOutOfStock() {
        Account buyer = buyer();
        Product product = outOfStockProduct();
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
        given(subscriptionRepository.existsByBuyer_IdAndProduct_Id(any(), eq(PRODUCT_ID)))
                .willReturn(false);
        given(subscriptionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        StockSubscriptionResponse result = service.subscribe(BUYER_EMAIL, PRODUCT_ID);

        assertThat(result.getProductName()).isEqualTo("Aquarelle test");
        then(subscriptionRepository).should().save(any(BackInStockSubscription.class));
    }

    /** subscribe must throw ProductInStockException when the product has stock. */
    @Test
    void subscribe_throwsProductInStockException_whenProductHasStock() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(inStockProduct()));

        assertThatThrownBy(() -> service.subscribe(BUYER_EMAIL, PRODUCT_ID))
                .isInstanceOf(ProductInStockException.class);
        then(subscriptionRepository).should(never()).save(any());
    }

    /** subscribe must throw AlreadySubscribedException when a subscription already exists. */
    @Test
    void subscribe_throwsAlreadySubscribedException_whenAlreadySubscribed() {
        Account buyer = buyer();
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(outOfStockProduct()));
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
        given(subscriptionRepository.existsByBuyer_IdAndProduct_Id(any(), eq(PRODUCT_ID)))
                .willReturn(true);

        assertThatThrownBy(() -> service.subscribe(BUYER_EMAIL, PRODUCT_ID))
                .isInstanceOf(AlreadySubscribedException.class);
        then(subscriptionRepository).should(never()).save(any());
    }

    /** subscribe must throw ProductNotFoundException when product does not exist. */
    @Test
    void subscribe_throwsProductNotFoundException_whenProductMissing() {
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.subscribe(BUYER_EMAIL, PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class);
    }

    /** unsubscribe must delete the subscription when it exists. */
    @Test
    void unsubscribe_deletesSubscription_whenFound() {
        Account buyer = buyer();
        BackInStockSubscription sub = new BackInStockSubscription();
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
        given(subscriptionRepository.findByBuyer_IdAndProduct_Id(any(), eq(PRODUCT_ID)))
                .willReturn(Optional.of(sub));

        service.unsubscribe(BUYER_EMAIL, PRODUCT_ID);

        then(subscriptionRepository).should().delete(sub);
    }

    /** unsubscribe must throw StockSubscriptionNotFoundException when no subscription exists. */
    @Test
    void unsubscribe_throwsNotFound_whenSubscriptionMissing() {
        Account buyer = buyer();
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
        given(subscriptionRepository.findByBuyer_IdAndProduct_Id(any(), eq(PRODUCT_ID)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.unsubscribe(BUYER_EMAIL, PRODUCT_ID))
                .isInstanceOf(StockSubscriptionNotFoundException.class);
        then(subscriptionRepository).should(never()).delete(any());
    }

    /** listSubscriptions must return all pending subscriptions for the buyer. */
    @Test
    void listSubscriptions_returnsBuyerSubscriptions() {
        Account buyer = buyer();
        Product product = outOfStockProduct();
        BackInStockSubscription sub = new BackInStockSubscription();
        sub.setBuyer(buyer);
        sub.setProduct(product);

        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
        given(subscriptionRepository.findByBuyer_IdOrderByCreatedAtDesc(any()))
                .willReturn(List.of(sub));

        List<StockSubscriptionResponse> result = service.listSubscriptions(BUYER_EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Aquarelle test");
    }

    /** listSubscriptions must return an empty list when the buyer has no subscriptions. */
    @Test
    void listSubscriptions_returnsEmpty_whenNoSubscriptions() {
        Account buyer = buyer();
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buyer));
        given(subscriptionRepository.findByBuyer_IdOrderByCreatedAtDesc(any()))
                .willReturn(List.of());

        List<StockSubscriptionResponse> result = service.listSubscriptions(BUYER_EMAIL);

        assertThat(result).isEmpty();
    }
}
