package com.shop.account.service;

import org.springframework.web.multipart.MultipartFile;

/** Stores and manages the vendor shop logo on the local filesystem (FS-V16). */
public interface VendorLogoUploadService {

    /**
     * Validates, stores, and returns the fixed public URL of the logo.
     * The filename is always {@code vendor-logo.png}; the previous file is replaced.
     * Accepted types: JPEG, PNG, WebP.
     *
     * @param file the uploaded image file
     * @return the public URL under which the logo is accessible
     */
    String store(MultipartFile file);

    /**
     * Returns the public URL of the current logo, or {@code null} if no logo has been uploaded.
     *
     * @return the logo URL, or {@code null}
     */
    String getPublicLogoUrl();

    /**
     * Deletes the current logo file if it exists.
     */
    void delete();
}
