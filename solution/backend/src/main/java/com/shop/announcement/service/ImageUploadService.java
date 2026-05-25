package com.shop.announcement.service;

import com.shop.announcement.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/** Handles storage and orientation detection of uploaded announcement images (US-ANN-01). */
public interface ImageUploadService {

    /**
     * Stores the uploaded image file and detects its orientation from pixel dimensions.
     * Accepted MIME types: image/jpeg, image/png, image/gif, image/webp.
     *
     * @param file the uploaded image file
     * @return the public URL and detected orientation
     * @throws IllegalArgumentException if the file is not a supported image type or cannot be read
     */
    ImageUploadResponse store(MultipartFile file);
}
