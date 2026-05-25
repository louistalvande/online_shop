package com.shop.announcement.controller;

import com.shop.announcement.dto.AnnouncementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/** Public endpoint serving active announcements to the buyer portal carousel (US-ANN-01). */
@Tag(name = "Announcements", description = "Public carousel announcements for the buyer portal")
@RequestMapping("/api/announcements")
public interface PublicAnnouncementController {

    /**
     * Returns all active announcements in ascending sort order.
     * No authentication required — used by the buyer portal home page.
     *
     * @return list of active announcements
     */
    @Operation(summary = "List active announcements for the buyer portal carousel")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping
    ResponseEntity<List<AnnouncementResponse>> listActive();
}
