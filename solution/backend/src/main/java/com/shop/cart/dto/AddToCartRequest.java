package com.shop.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Request payload to add a product to the cart (US-CRT-01). */
public class AddToCartRequest {

    /** @return the UUID of the product to add */
    @Schema(description = "UUID of the product to add")
    @NotNull
    private UUID productId;

    /** @return the number of units to add (must be at least 1) */
    @Schema(description = "Number of units to add (minimum 1)")
    @Min(1)
    private int quantity = 1;

    /** @return the product UUID */
    public UUID getProductId() { return productId; }

    /** @param productId the product UUID */
    public void setProductId(UUID productId) { this.productId = productId; }

    /** @return the quantity to add */
    public int getQuantity() { return quantity; }

    /** @param quantity the quantity to add */
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
