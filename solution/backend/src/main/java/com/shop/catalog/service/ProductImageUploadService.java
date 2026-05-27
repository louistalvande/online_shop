package com.shop.catalog.service;

import com.shop.catalog.dto.ProductImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/** Handles storage of uploaded product images (US-CAT-09). */
public interface ProductImageUploadService {

    /**
     * Stores the uploaded image file on the filesystem and returns its public URL.
     * Accepted MIME types: image/jpeg, image/png, image/gif, image/webp.
     *
     * @param file the uploaded image file
     * @return the public URL of the stored image
     * @throws IllegalArgumentException if the file is not a supported image type
     */
    ProductImageUploadResponse store(MultipartFile file);
}
