package com.shop.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/** Request payload to update the quantity of an existing cart item (US-CRT-01). */
public class UpdateCartItemRequest {

    /** @return the new quantity for the cart item (must be at least 1) */
    @Schema(description = "New quantity for the cart item (minimum 1)")
    @Min(1)
    private int quantity;

    /** @return the new quantity */
    public int getQuantity() { return quantity; }

    /** @param quantity the new quantity */
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
