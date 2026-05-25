package com.shop.announcement.controller.impl;

import com.shop.announcement.controller.PublicAnnouncementController;
import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** {@link PublicAnnouncementController} implementation. */
@RestController
public class PublicAnnouncementControllerImpl implements PublicAnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * Constructs the controller with the announcement service.
     *
     * @param announcementService the announcement business service
     */
    public PublicAnnouncementControllerImpl(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<AnnouncementResponse>> listActive() {
        return ResponseEntity.ok(announcementService.listActive());
    }
}
