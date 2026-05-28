package com.shop.account.service.impl;

import com.shop.account.service.VendorBannerUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/** {@link VendorBannerUploadService} implementation — stores the vendor hero banner on the local filesystem. */
@Service
public class VendorBannerUploadServiceImpl implements VendorBannerUploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final String FILENAME = "vendor-banner.png";

    private final Path uploadDir;
    private final String publicBasePath;

    /**
     * Constructs the service and ensures the banner upload directory exists.
     *
     * @param uploadDirPath  filesystem path to the banner upload directory
     * @param publicBasePath public URL base path for stored banners
     */
    public VendorBannerUploadServiceImpl(
            @Value("${app.vendor-banner-upload-dir:uploads/banners}") String uploadDirPath,
            @Value("${app.vendor-banner-upload-base-url:/uploads/banners}") String publicBasePath) {
        this.uploadDir = Paths.get(uploadDirPath);
        this.publicBasePath = publicBasePath;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create vendor banner upload directory: " + uploadDirPath, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported banner image type: " + contentType);
        }

        // Fixed filename so the public URL never changes when the banner is replaced (FS-V16).
        Path destination = uploadDir.resolve(FILENAME);

        try {
            Files.deleteIfExists(destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clear previous vendor banner", e);
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store vendor banner", e);
        }

        try {
            if ("image/webp".equals(contentType)) {
                if (!hasWebpMagicBytes(destination)) {
                    Files.deleteIfExists(destination);
                    throw new IllegalArgumentException("Unsupported banner image type: " + contentType);
                }
            } else {
                BufferedImage img = ImageIO.read(destination.toFile());
                if (img == null) {
                    Files.deleteIfExists(destination);
                    throw new IllegalArgumentException("Unsupported banner image type: " + contentType);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate vendor banner: " + FILENAME, e);
        }

        return publicBasePath + "/" + FILENAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getPublicBannerUrl() {
        return Files.exists(uploadDir.resolve(FILENAME)) ? publicBasePath + "/" + FILENAME : null;
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        try {
            Files.deleteIfExists(uploadDir.resolve(FILENAME));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete vendor banner", e);
        }
    }

    private boolean hasWebpMagicBytes(Path path) throws IOException {
        byte[] header = new byte[12];
        try (InputStream in = Files.newInputStream(path)) {
            if (in.read(header) < 12) return false;
        }
        return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }
}
