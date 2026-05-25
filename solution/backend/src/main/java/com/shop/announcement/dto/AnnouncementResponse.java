package com.shop.announcement.dto;

import com.shop.announcement.entity.Announcement;
import com.shop.announcement.entity.AnnouncementContentType;
import com.shop.announcement.entity.ImageOrientation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/** Read model for a scrolling announcement (US-ANN-01). */
public class AnnouncementResponse {

    @Schema(description = "Announcement UUID")
    private UUID id;

    @Schema(description = "Vendor account UUID")
    private UUID vendorId;

    @Schema(description = "Content type: TEXT, IMAGE, or BOTH")
    private AnnouncementContentType contentType;

    @Schema(description = "Text body, or null for image-only announcements")
    private String textContent;

    @Schema(description = "Image URL, or null for text-only announcements")
    private String imageUrl;

    @Schema(description = "Auto-detected image orientation (PORTRAIT or LANDSCAPE), null when no image")
    private ImageOrientation imageOrientation;

    @Schema(description = "Optional redirect URL")
    private String redirectUrl;

    @Schema(description = "Display sort order (0 = first)")
    private int sortOrder;

    @Schema(description = "Whether the announcement is visible in the carousel")
    private boolean active;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last-modified timestamp")
    private LocalDateTime updatedAt;

    /**
     * Maps an {@link Announcement} entity to its response DTO.
     *
     * @param a the announcement entity
     * @return the corresponding response DTO
     */
    public static AnnouncementResponse from(Announcement a) {
        AnnouncementResponse r = new AnnouncementResponse();
        r.id               = a.getId();
        r.vendorId         = a.getVendor().getId();
        r.contentType      = a.getContentType();
        r.textContent      = a.getTextContent();
        r.imageUrl         = a.getImageUrl();
        r.imageOrientation = a.getImageOrientation();
        r.redirectUrl      = a.getRedirectUrl();
        r.sortOrder        = a.getSortOrder();
        r.active           = a.isActive();
        r.createdAt        = a.getCreatedAt();
        r.updatedAt        = a.getUpdatedAt();
        return r;
    }

    /** @return the announcement UUID */
    public UUID getId() { return id; }

    /** @return the vendor account UUID */
    public UUID getVendorId() { return vendorId; }

    /** @return the content type */
    public AnnouncementContentType getContentType() { return contentType; }

    /** @return the text content, or null */
    public String getTextContent() { return textContent; }

    /** @return the image URL, or null */
    public String getImageUrl() { return imageUrl; }

    /** @return the auto-detected image orientation, or null */
    public ImageOrientation getImageOrientation() { return imageOrientation; }

    /** @return the optional redirect URL */
    public String getRedirectUrl() { return redirectUrl; }

    /** @return the display sort order */
    public int getSortOrder() { return sortOrder; }

    /** @return true if the announcement is active */
    public boolean isActive() { return active; }

    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return the last-modified timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
