package com.shop.account.service.impl;

import com.shop.account.exception.UnsupportedLogoImageTypeException;
import com.shop.account.service.VendorLogoUploadService;
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

/** {@link VendorLogoUploadService} implementation — stores vendor logo images on the local filesystem. */
@Service
public class VendorLogoUploadServiceImpl implements VendorLogoUploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final Path uploadDir;
    private final String publicBasePath;

    /**
     * Constructs the service and ensures the logo upload directory exists.
     *
     * @param uploadDirPath  filesystem path to the logo upload directory
     * @param publicBasePath public URL base path for stored logos
     */
    public VendorLogoUploadServiceImpl(
            @Value("${app.vendor-logo-upload-dir:uploads/logos}") String uploadDirPath,
            @Value("${app.vendor-logo-upload-base-url:/uploads/logos}") String publicBasePath) {
        this.uploadDir = Paths.get(uploadDirPath);
        this.publicBasePath = publicBasePath;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create vendor logo upload directory: " + uploadDirPath, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String store(MultipartFile file) {
        String contentType = resolveContentType(file);
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new UnsupportedLogoImageTypeException(contentType);
        }

        // Fixed filename so the public URL never changes when the logo is replaced (FS-V16).
        String filename = "vendor-logo.png";
        Path destination = uploadDir.resolve(filename);

        try {
            Files.deleteIfExists(destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clear previous vendor logo", e);
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store vendor logo", e);
        }

        try {
            if ("image/webp".equals(contentType)) {
                if (!hasWebpMagicBytes(destination)) {
                    Files.deleteIfExists(destination);
                    throw new UnsupportedLogoImageTypeException(contentType);
                }
            } else {
                BufferedImage img = ImageIO.read(destination.toFile());
                if (img == null) {
                    Files.deleteIfExists(destination);
                    throw new UnsupportedLogoImageTypeException(contentType);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate vendor logo: " + filename, e);
        }

        return publicBasePath + "/" + filename;
    }

    /** {@inheritDoc} */
    @Override
    public String getPublicLogoUrl() {
        Path logoFile = uploadDir.resolve("vendor-logo.png");
        return Files.exists(logoFile) ? publicBasePath + "/vendor-logo.png" : null;
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        try {
            Files.deleteIfExists(uploadDir.resolve("vendor-logo.png"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete vendor logo", e);
        }
    }

    private String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null && !ct.isBlank()) return ct;
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        return null;
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
