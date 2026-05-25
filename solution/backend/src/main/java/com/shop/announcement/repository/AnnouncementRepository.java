package com.shop.announcement.repository;

import com.shop.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** JPA repository for {@link Announcement} entities (US-ANN-01). */
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    /**
     * Returns all announcements belonging to the given vendor, ordered by sort position.
     *
     * @param vendorId the vendor account UUID
     * @return announcements sorted by sort_order ascending
     */
    List<Announcement> findByVendorIdOrderBySortOrderAsc(UUID vendorId);

    /**
     * Returns all active announcements across all vendors, ordered by sort position.
     * Used to populate the buyer portal carousel.
     *
     * @return active announcements sorted by sort_order ascending
     */
    List<Announcement> findByActiveTrueOrderBySortOrderAsc();
}
