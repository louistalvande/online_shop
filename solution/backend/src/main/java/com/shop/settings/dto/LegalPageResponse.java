package com.shop.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO carrying the content of a legal page. */
public class LegalPageResponse {

    @Schema(description = "Identifier of the legal page (e.g. legal_cgv)")
    private final String key;

    @Schema(description = "Plain-text content of the legal page")
    private final String content;

    /**
     * @param key     the platform-setting key identifying the legal page
     * @param content the plain-text body of the page
     */
    public LegalPageResponse(String key, String content) {
        this.key = key;
        this.content = content;
    }

    /** @return the page key */
    public String getKey() { return key; }

    /** @return the page content */
    public String getContent() { return content; }
}
