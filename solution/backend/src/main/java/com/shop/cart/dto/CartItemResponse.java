package com.shop.cart.dto;

import com.shop.cart.entity.CartItem;
import com.shop.catalog.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/** Response DTO for a single cart line item (US-CRT-01). */
public class CartItemResponse {

    private static final BigDecimal VAT_RATE = new BigDecimal("1.20");

    /** @return the cart item UUID */
    @Schema(description = "Cart item UUID")
    private UUID id;

    /** @return the product UUID */
    @Schema(description = "Product UUID")
    private UUID productId;

    /** @return the product display name */
    @Schema(description = "Product display name")
    private String productName;

    /** @return the product pre-tax unit price */
    @Schema(description = "Product unit price excluding VAT")
    private BigDecimal priceExclTax;

    /** @return the product unit price including 20% VAT */
    @Schema(description = "Product unit price including 20% VAT")
    private BigDecimal priceTTC;

    /** @return the URL of the first product photo, or null if none */
    @Schema(description = "URL of the first product photo, null if none")
    private String photoUrl;

    /** @return true when the product still has stock available */
    @Schema(description = "Whether the product currently has stock available")
    private boolean inStock;

    /** @return the current quantity requested in this line */
    @Schema(description = "Quantity in this line")
    private int quantity;

    /** @return the total price for this line (priceTTC × quantity) */
    @Schema(description = "Total price for this line (priceTTC × quantity)")
    private BigDecimal lineTotal;

    private CartItemResponse() {}

    /**
     * Builds a response from a {@link CartItem} entity.
     *
     * @param item the cart item
     * @return the populated response DTO
     */
    public static CartItemResponse from(CartItem item) {
        CartItemResponse r = new CartItemResponse();
        r.id = item.getId();
        r.productId = item.getProduct().getId();
        r.productName = item.getProduct().getName();
        r.priceExclTax = item.getProduct().getPriceExclTax();
        r.priceTTC = item.getProduct().getPriceExclTax()
                .multiply(VAT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        r.photoUrl = item.getProduct().getPhotos().isEmpty()
                ? null
                : item.getProduct().getPhotos().get(0).getUrl();
        r.inStock = item.getProduct().getStatus() == ProductStatus.PUBLISHED
                && item.getProduct().getQuantity() > 0;
        r.quantity = item.getQuantity();
        r.lineTotal = r.priceTTC
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return r;
    }

    /** @return the cart item UUID */
    public UUID getId() { return id; }

    /** @return the product UUID */
    public UUID getProductId() { return productId; }

    /** @return the product display name */
    public String getProductName() { return productName; }

    /** @return the pre-tax unit price */
    public BigDecimal getPriceExclTax() { return priceExclTax; }

    /** @return the unit price including VAT */
    public BigDecimal getPriceTTC() { return priceTTC; }

    /** @return the first photo URL, or null */
    public String getPhotoUrl() { return photoUrl; }

    /** @return whether the product currently has available stock */
    public boolean isInStock() { return inStock; }

    /** @return the requested quantity */
    public int getQuantity() { return quantity; }

    /** @return the line total (priceTTC × quantity) */
    public BigDecimal getLineTotal() { return lineTotal; }
}
