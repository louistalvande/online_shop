package com.shop.order.dto;

import com.shop.order.entity.OrderLine;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/** Response DTO for one order line. */
public class OrderLineResponse {

    @Schema(description = "Line UUID") private UUID id;
    @Schema(description = "Product UUID, null if product was deleted") private UUID productId;
    @Schema(description = "Product name snapshot") private String productName;
    @Schema(description = "Unit price excluding VAT") private BigDecimal unitPriceExclTax;
    @Schema(description = "Unit price including VAT") private BigDecimal unitPriceTtc;
    @Schema(description = "Quantity ordered") private int quantity;
    @Schema(description = "Line total including VAT") private BigDecimal lineTotalTtc;

    private OrderLineResponse() {}

    /**
     * Builds a response from an {@link OrderLine} entity.
     *
     * @param line the order line entity
     * @return the populated response DTO
     */
    public static OrderLineResponse from(OrderLine line) {
        OrderLineResponse r = new OrderLineResponse();
        r.id = line.getId();
        r.productId = line.getProductId();
        r.productName = line.getProductName();
        r.unitPriceExclTax = line.getUnitPriceExclTax();
        r.unitPriceTtc = line.getUnitPriceTtc();
        r.quantity = line.getQuantity();
        r.lineTotalTtc = line.getLineTotalTtc();
        return r;
    }

    /** @return the line UUID */
    public UUID getId() { return id; }
    /** @return the product UUID */
    public UUID getProductId() { return productId; }
    /** @return the product name */
    public String getProductName() { return productName; }
    /** @return the unit price excluding VAT */
    public BigDecimal getUnitPriceExclTax() { return unitPriceExclTax; }
    /** @return the unit price including VAT */
    public BigDecimal getUnitPriceTtc() { return unitPriceTtc; }
    /** @return the quantity ordered */
    public int getQuantity() { return quantity; }
    /** @return the line total including VAT */
    public BigDecimal getLineTotalTtc() { return lineTotalTtc; }
}
