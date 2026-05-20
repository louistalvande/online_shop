package com.shop.cart.dto;

import com.shop.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Response DTO for the buyer's full cart (US-CRT-01, US-CRT-02). */
public class CartResponse {

    /** @return the cart UUID */
    @Schema(description = "Cart UUID")
    private UUID id;

    /** @return the buyer account UUID */
    @Schema(description = "Buyer account UUID")
    private UUID buyerId;

    /** @return the ordered list of line items */
    @Schema(description = "Line items")
    private List<CartItemResponse> items;

    /** @return the grand total of all lines including VAT */
    @Schema(description = "Grand total of all line items including VAT")
    private BigDecimal total;

    /** @return the timestamp of the last cart modification */
    @Schema(description = "Timestamp of the last cart modification")
    private LocalDateTime updatedAt;

    private CartResponse() {}

    /**
     * Builds a response from a {@link Cart} entity.
     *
     * @param cart the cart entity
     * @return the populated response DTO
     */
    public static CartResponse from(Cart cart) {
        CartResponse r = new CartResponse();
        r.id = cart.getId();
        r.buyerId = cart.getBuyerId();
        r.items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        r.total = r.items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        r.updatedAt = cart.getUpdatedAt();
        return r;
    }

    /** @return the cart UUID */
    public UUID getId() { return id; }

    /** @return the buyer account UUID */
    public UUID getBuyerId() { return buyerId; }

    /** @return the line items */
    public List<CartItemResponse> getItems() { return items; }

    /** @return the grand total */
    public BigDecimal getTotal() { return total; }

    /** @return the last-updated timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
