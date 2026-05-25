package com.shop.announcement.controller;

import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.dto.CreateAnnouncementRequest;
import com.shop.announcement.dto.UpdateAnnouncementRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Vendor endpoints for managing scrolling announcements (US-ANN-01). */
@Tag(name = "Vendor Announcements", description = "CRUD and reordering of vendor announcements")
@RequestMapping("/api/vendor/announcements")
public interface VendorAnnouncementController {

    /**
     * Lists all announcements for the authenticated vendor.
     *
     * @param principal the authenticated vendor
     * @return ordered list of announcements
     */
    @Operation(summary = "List all announcements for the authenticated vendor")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping
    ResponseEntity<List<AnnouncementResponse>> list(Principal principal);

    /**
     * Creates a new announcement.
     *
     * @param principal the authenticated vendor
     * @param request   the creation request
     * @return the created announcement
     */
    @Operation(summary = "Create a new announcement")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Announcement created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<AnnouncementResponse> create(Principal principal,
                                                @RequestBody @Valid CreateAnnouncementRequest request);

    /**
     * Updates an existing announcement owned by the authenticated vendor.
     *
     * @param principal the authenticated vendor
     * @param id        the announcement UUID
     * @param request   the update request
     * @return the updated announcement
     */
    @Operation(summary = "Update an announcement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Announcement updated"),
        @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<AnnouncementResponse> update(Principal principal,
                                                @PathVariable UUID id,
                                                @RequestBody @Valid UpdateAnnouncementRequest request);

    /**
     * Deletes an announcement owned by the authenticated vendor.
     *
     * @param principal the authenticated vendor
     * @param id        the announcement UUID
     * @return 204 No Content
     */
    @Operation(summary = "Delete an announcement")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Announcement deleted"),
        @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(Principal principal, @PathVariable UUID id);

    /**
     * Moves an announcement one position up (lower sort_order).
     *
     * @param principal the authenticated vendor
     * @param id        the announcement UUID
     * @return the updated announcement
     */
    @Operation(summary = "Move announcement one position up")
    @ApiResponse(responseCode = "200", description = "Announcement moved")
    @PatchMapping("/{id}/move-up")
    ResponseEntity<AnnouncementResponse> moveUp(Principal principal, @PathVariable UUID id);

    /**
     * Moves an announcement one position down (higher sort_order).
     *
     * @param principal the authenticated vendor
     * @param id        the announcement UUID
     * @return the updated announcement
     */
    @Operation(summary = "Move announcement one position down")
    @ApiResponse(responseCode = "200", description = "Announcement moved")
    @PatchMapping("/{id}/move-down")
    ResponseEntity<AnnouncementResponse> moveDown(Principal principal, @PathVariable UUID id);
}
