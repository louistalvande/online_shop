package com.shop.announcement.entity;

/**
 * Orientation of an announcement image, auto-detected from its pixel dimensions.
 * LANDSCAPE when width &gt; height, PORTRAIT otherwise.
 */
public enum ImageOrientation {
    /** Width is strictly greater than height. */
    LANDSCAPE,
    /** Height is greater than or equal to width. */
    PORTRAIT
}
