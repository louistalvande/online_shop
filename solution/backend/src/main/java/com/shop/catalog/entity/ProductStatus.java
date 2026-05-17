package com.shop.catalog.entity;

/** Lifecycle status of a product in the catalog (US-CAT-01, US-CAT-03). */
public enum ProductStatus {
    /** Visible to buyers in the catalog. */
    PUBLISHED,
    /** Hidden from buyers; no new orders possible. */
    ARCHIVED
}
