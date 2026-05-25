package com.shop.announcement.controller.impl;

import com.shop.announcement.controller.ImageUploadController;
import com.shop.announcement.dto.ImageUploadResponse;
import com.shop.announcement.service.ImageUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** {@link ImageUploadController} implementation. */
@RestController
public class ImageUploadControllerImpl implements ImageUploadController {

    private final ImageUploadService imageUploadService;

    /**
     * Constructs the controller with the image upload service.
     *
     * @param imageUploadService the image upload and orientation detection service
     */
    public ImageUploadControllerImpl(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ImageUploadResponse> upload(MultipartFile file) {
        return ResponseEntity.ok(imageUploadService.store(file));
    }
}
