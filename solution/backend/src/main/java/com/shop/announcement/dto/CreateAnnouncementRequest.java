package com.shop.announcement.dto;

import com.shop.announcement.entity.AnnouncementContentType;
import com.shop.announcement.entity.ImageOrientation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request body for creating a scrolling announcement (US-ANN-01). */
public class CreateAnnouncementRequest {

    @NotNull
    @Schema(description = "Content type: TEXT, IMAGE, or BOTH")
    private AnnouncementContentType contentType;

    @Size(max = 500)
    @Schema(description = "Text body — required for TEXT and BOTH types")
    private String textContent;

    @Size(max = 500)
    @Schema(description = "Server-side path of the uploaded image, returned by the upload endpoint")
    private String imageUrl;

    @Schema(description = "Orientation detected server-side at upload time (PORTRAIT or LANDSCAPE)")
    private ImageOrientation imageOrientation;

    @Size(max = 500)
    @Schema(description = "Optional URL to redirect to when the announcement is clicked")
    private String redirectUrl;

    @Min(0)
    @Schema(description = "Display sort order (0 = first)")
    private int sortOrder = 0;

    @Schema(description = "Whether the announcement is visible in the carousel")
    private boolean active = true;

    /** @return the content type */
    public AnnouncementContentType getContentType() { return contentType; }

    /** @param contentType the content type */
    public void setContentType(AnnouncementContentType contentType) { this.contentType = contentType; }

    /** @return the text content */
    public String getTextContent() { return textContent; }

    /** @param textContent the text content */
    public void setTextContent(String textContent) { this.textContent = textContent; }

    /** @return the server-side image path */
    public String getImageUrl() { return imageUrl; }

    /** @param imageUrl the server-side image path */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** @return the orientation detected at upload time */
    public ImageOrientation getImageOrientation() { return imageOrientation; }

    /** @param imageOrientation the detected image orientation */
    public void setImageOrientation(ImageOrientation imageOrientation) { this.imageOrientation = imageOrientation; }

    /** @return the optional redirect URL */
    public String getRedirectUrl() { return redirectUrl; }

    /** @param redirectUrl the optional redirect URL */
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }

    /** @return the display sort order */
    public int getSortOrder() { return sortOrder; }

    /** @param sortOrder the display sort order */
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    /** @return true if the announcement should be visible */
    public boolean isActive() { return active; }

    /** @param active visibility flag */
    public void setActive(boolean active) { this.active = active; }
}
