package com.shop.announcement.controller.impl;

import com.shop.announcement.controller.VendorAnnouncementController;
import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.dto.CreateAnnouncementRequest;
import com.shop.announcement.dto.UpdateAnnouncementRequest;
import com.shop.announcement.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** {@link VendorAnnouncementController} implementation. */
@RestController
public class VendorAnnouncementControllerImpl implements VendorAnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * Constructs the controller with the announcement service.
     *
     * @param announcementService the announcement business service
     */
    public VendorAnnouncementControllerImpl(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<AnnouncementResponse>> list(Principal principal) {
        return ResponseEntity.ok(announcementService.listForVendor(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AnnouncementResponse> create(Principal principal,
                                                       CreateAnnouncementRequest request) {
        AnnouncementResponse response = announcementService.create(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AnnouncementResponse> update(Principal principal,
                                                       UUID id,
                                                       UpdateAnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.update(principal.getName(), id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> delete(Principal principal, UUID id) {
        announcementService.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AnnouncementResponse> moveUp(Principal principal, UUID id) {
        return ResponseEntity.ok(announcementService.moveUp(principal.getName(), id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AnnouncementResponse> moveDown(Principal principal, UUID id) {
        return ResponseEntity.ok(announcementService.moveDown(principal.getName(), id));
    }
}
