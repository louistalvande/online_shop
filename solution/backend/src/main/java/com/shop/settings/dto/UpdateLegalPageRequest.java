package com.shop.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request body to update the content of a legal page. */
public class UpdateLegalPageRequest {

    @NotBlank
    @Schema(description = "New plain-text content for the legal page")
    private String content;

    /** @return the new content */
    public String getContent() { return content; }

    /** @param content the new content */
    public void setContent(String content) { this.content = content; }
}
