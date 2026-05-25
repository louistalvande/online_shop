package com.shop.announcement.service;

import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.dto.CreateAnnouncementRequest;
import com.shop.announcement.dto.UpdateAnnouncementRequest;
import com.shop.announcement.exception.AnnouncementNotFoundException;

import java.util.List;
import java.util.UUID;

/** Business operations for scrolling announcements (US-ANN-01). */
public interface AnnouncementService {

    /**
     * Creates a new announcement owned by the given vendor.
     * Image orientation is auto-detected from imageWidth and imageHeight when present.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param request     the creation request
     * @return the created announcement
     */
    AnnouncementResponse create(String vendorEmail, CreateAnnouncementRequest request);

    /**
     * Returns all announcements belonging to the given vendor, sorted by sort_order.
     *
     * @param vendorEmail the authenticated vendor's email
     * @return list of announcements sorted ascending by sort order
     */
    List<AnnouncementResponse> listForVendor(String vendorEmail);

    /**
     * Updates an existing announcement owned by the given vendor.
     * Image orientation is recomputed when imageWidth and imageHeight are provided.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param id          the announcement UUID
     * @param request     the update request
     * @return the updated announcement
     * @throws AnnouncementNotFoundException if the announcement does not exist or belongs to another vendor
     */
    AnnouncementResponse update(String vendorEmail, UUID id, UpdateAnnouncementRequest request);

    /**
     * Deletes an announcement owned by the given vendor.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param id          the announcement UUID
     * @throws AnnouncementNotFoundException if the announcement does not exist or belongs to another vendor
     */
    void delete(String vendorEmail, UUID id);

    /**
     * Moves an announcement one position up in the vendor's list (decreases sort_order).
     * Does nothing if the announcement is already first.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param id          the announcement UUID
     * @return the updated announcement
     * @throws AnnouncementNotFoundException if the announcement does not exist or belongs to another vendor
     */
    AnnouncementResponse moveUp(String vendorEmail, UUID id);

    /**
     * Moves an announcement one position down in the vendor's list (increases sort_order).
     * Does nothing if the announcement is already last.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param id          the announcement UUID
     * @return the updated announcement
     * @throws AnnouncementNotFoundException if the announcement does not exist or belongs to another vendor
     */
    AnnouncementResponse moveDown(String vendorEmail, UUID id);

    /**
     * Returns all active announcements across all vendors, sorted by sort_order.
     * Used by the public buyer portal carousel.
     *
     * @return list of active announcements
     */
    List<AnnouncementResponse> listActive();
}
