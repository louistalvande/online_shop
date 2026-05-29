package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Request body for creating a new product (US-CAT-01). */
public class CreateProductRequest {

    @Schema(description = "Product display name", example = "Aquarelle forêt d'automne")
    @NotBlank
    @Size(max = 200)
    private String name;

    @Schema(description = "Optional detailed description of the product")
    private String description;

    @Schema(description = "Pre-tax unit price in euros", example = "29.90")
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal priceExclTax;

    @Schema(description = "Product type (poster, carte, affiche…)", example = "Poster")
    @Size(max = 100)
    private String category;

    @Schema(description = "Product theme or occasion (paysage, naissance, vacances…)", example = "Paysage")
    @Size(max = 100)
    private String theme;

    @Schema(description = "Initial available quantity in stock", example = "10")
    @NotNull
    @Min(0)
    private Integer quantity;

    @Schema(description = "Stock alert threshold — alert triggered when quantity falls below this value", example = "3")
    @Min(0)
    private int stockAlertThreshold = 0;

    @Schema(description = "List of publicly accessible photo URLs")
    private List<String> photoUrls = new ArrayList<>();

    /** @return the product name */
    public String getName() { return name; }

    /** @param name the product name */
    public void setName(String name) { this.name = name; }

    /** @return the product description */
    public String getDescription() { return description; }

    /** @param description the product description */
    public void setDescription(String description) { this.description = description; }

    /** @return the pre-tax price */
    public BigDecimal getPriceExclTax() { return priceExclTax; }

    /** @param priceExclTax the pre-tax price */
    public void setPriceExclTax(BigDecimal priceExclTax) { this.priceExclTax = priceExclTax; }

    /** @return the product type */
    public String getCategory() { return category; }

    /** @param category the product type */
    public void setCategory(String category) { this.category = category; }

    /** @return the product theme or occasion */
    public String getTheme() { return theme; }

    /** @param theme the product theme or occasion */
    public void setTheme(String theme) { this.theme = theme; }

    /** @return the initial quantity */
    public Integer getQuantity() { return quantity; }

    /** @param quantity the initial quantity */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /** @return the stock alert threshold */
    public int getStockAlertThreshold() { return stockAlertThreshold; }

    /** @param stockAlertThreshold the stock alert threshold */
    public void setStockAlertThreshold(int stockAlertThreshold) { this.stockAlertThreshold = stockAlertThreshold; }

    /** @return the list of photo URLs */
    public List<String> getPhotoUrls() { return photoUrls; }

    /** @param photoUrls the list of photo URLs */
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
}
