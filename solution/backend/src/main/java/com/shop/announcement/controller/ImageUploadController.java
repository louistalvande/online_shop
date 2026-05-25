package com.shop.announcement.controller;

import com.shop.announcement.dto.ImageUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Vendor endpoint for uploading announcement images (US-ANN-01).
 * The server detects orientation server-side from the image dimensions.
 */
@Tag(name = "Vendor Announcements", description = "Image upload for announcement creation")
@RequestMapping("/api/vendor/announcements/images")
public interface ImageUploadController {

    /**
     * Uploads an image file, stores it server-side, and returns the public URL with the
     * auto-detected orientation (PORTRAIT if height &ge; width, LANDSCAPE if width &gt; height).
     *
     * @param file the image file to upload (JPEG, PNG, GIF, or WebP)
     * @return the public URL and detected orientation
     */
    @Operation(summary = "Upload an announcement image and detect its orientation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image stored — URL and orientation returned"),
        @ApiResponse(responseCode = "400", description = "Unsupported file type or unreadable image")
    })
    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file);
}
