package com.shop.account.service;

import org.springframework.web.multipart.MultipartFile;

/** Stores and manages the vendor shop hero banner on the local filesystem (FS-V16). */
public interface VendorBannerUploadService {

    /**
     * Validates, stores, and returns the fixed public URL of the banner.
     * The filename is always {@code vendor-banner.png}; the previous file is replaced.
     * Accepted types: JPEG, PNG, WebP.
     *
     * @param file the uploaded image file
     * @return the public URL under which the banner is accessible
     */
    String store(MultipartFile file);

    /**
     * Returns the public URL of the current banner, or {@code null} if no banner has been uploaded.
     *
     * @return the banner URL, or {@code null}
     */
    String getPublicBannerUrl();

    /**
     * Deletes the current banner file if it exists.
     */
    void delete();
}
