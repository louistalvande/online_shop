package com.shop.announcement.exception;

import java.util.UUID;

/** Thrown when an announcement cannot be found or does not belong to the requesting vendor. */
public class AnnouncementNotFoundException extends RuntimeException {

    /**
     * Constructs the exception for a given announcement UUID.
     *
     * @param id the announcement UUID that was not found
     */
    public AnnouncementNotFoundException(UUID id) {
        super("Announcement not found: " + id);
    }
}
