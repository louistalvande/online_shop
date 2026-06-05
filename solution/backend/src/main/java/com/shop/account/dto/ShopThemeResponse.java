package com.shop.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Public shop theme returned to unauthenticated clients (FS-V16). */
public class ShopThemeResponse {

    @Schema(description = "Shop name displayed in the buyer-facing header; never null")
    private String shopName;

    @Schema(description = "CSS hex accent colour for --accent; never null — defaults to the platform value")
    private String accentColor;

    @Schema(description = "CSS hex background colour for --bg; never null — defaults to the platform value")
    private String bgColor;

    @Schema(description = "Public URL of the vendor logo file; null if no logo has been uploaded")
    private String logoUrl;

    @Schema(description = "Public URL of the vendor hero banner; null if no banner has been uploaded")
    private String bannerUrl;

    /**
     * @param shopName    the shop name
     * @param accentColor the accent colour
     * @param bgColor     the background colour
     * @param logoUrl     the logo URL, or {@code null} if none uploaded
     * @param bannerUrl   the hero banner URL, or {@code null} if none uploaded
     */
    public ShopThemeResponse(String shopName, String accentColor, String bgColor, String logoUrl, String bannerUrl) {
        this.shopName    = shopName;
        this.accentColor = accentColor;
        this.bgColor     = bgColor;
        this.logoUrl     = logoUrl;
        this.bannerUrl   = bannerUrl;
    }

    /** @return the shop name */
    public String getShopName() { return shopName; }

    /** @return the accent colour */
    public String getAccentColor() { return accentColor; }

    /** @return the background colour */
    public String getBgColor() { return bgColor; }

    /** @return the logo URL, or {@code null} */
    public String getLogoUrl() { return logoUrl; }

    /** @return the hero banner URL, or {@code null} */
    public String getBannerUrl() { return bannerUrl; }
}
