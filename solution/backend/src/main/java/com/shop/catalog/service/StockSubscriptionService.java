package com.shop.catalog.service;

import com.shop.catalog.dto.StockSubscriptionResponse;

import java.util.List;
import java.util.UUID;

/** Manages buyer back-in-stock alert subscriptions (US-SHP-03 / FS-B14). */
public interface StockSubscriptionService {

    /**
     * Subscribes the authenticated buyer to a back-in-stock alert for the given product.
     * Throws {@link com.shop.catalog.exception.ProductInStockException} if the product is in stock.
     * Throws {@link com.shop.catalog.exception.AlreadySubscribedException} if an active subscription exists.
     * Throws {@link com.shop.catalog.exception.ProductNotFoundException} if the product does not exist.
     *
     * @param buyerEmail the email of the authenticated buyer
     * @param productId  the UUID of the out-of-stock product
     * @return the created subscription
     */
    StockSubscriptionResponse subscribe(String buyerEmail, UUID productId);

    /**
     * Cancels the buyer's active subscription for the given product.
     * Throws {@link com.shop.catalog.exception.StockSubscriptionNotFoundException} if no subscription exists.
     *
     * @param buyerEmail the email of the authenticated buyer
     * @param productId  the UUID of the subscribed product
     */
    void unsubscribe(String buyerEmail, UUID productId);

    /**
     * Returns all active (not-yet-notified) subscriptions for the authenticated buyer.
     *
     * @param buyerEmail the email of the authenticated buyer
     * @return list of active subscriptions, newest first
     */
    List<StockSubscriptionResponse> listSubscriptions(String buyerEmail);
}
