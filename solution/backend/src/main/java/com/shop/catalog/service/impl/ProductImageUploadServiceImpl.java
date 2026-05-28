package com.shop.catalog.service.impl;

import com.shop.catalog.dto.ProductImageUploadResponse;
import com.shop.catalog.exception.UnsupportedProductImageTypeException;
import com.shop.catalog.service.ProductImageUploadService;
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
import java.util.UUID;

/** {@link ProductImageUploadService} implementation — stores product images on the local filesystem. */
@Service
public class ProductImageUploadServiceImpl implements ProductImageUploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final Path uploadDir;
    private final String publicBasePath;

    /**
     * Constructs the service and ensures the product image upload directory exists.
     *
     * @param uploadDirPath  filesystem path to the product image upload directory
     * @param publicBasePath public URL base path for stored product images
     */
    public ProductImageUploadServiceImpl(
            @Value("${app.product-upload-dir:uploads/products}") String uploadDirPath,
            @Value("${app.product-upload-base-url:/uploads/products}") String publicBasePath) {
        this.uploadDir = Paths.get(uploadDirPath);
        this.publicBasePath = publicBasePath;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create product upload directory: " + uploadDirPath, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ProductImageUploadResponse store(MultipartFile file) {
        // First-pass: reject based on declared MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new UnsupportedProductImageTypeException(contentType);
        }

        String extension = extensionFor(contentType);
        String filename = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store product image: " + filename, e);
        }

        // Second-pass: validate actual file bytes — rejects files whose content doesn't match the declared type
        try {
            if ("image/webp".equals(contentType)) {
                if (!hasWebpMagicBytes(destination)) {
                    Files.deleteIfExists(destination);
                    throw new UnsupportedProductImageTypeException(contentType);
                }
            } else {
                BufferedImage img = ImageIO.read(destination.toFile());
                if (img == null) {
                    Files.deleteIfExists(destination);
                    throw new UnsupportedProductImageTypeException(contentType);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate product image: " + filename, e);
        }

        return new ProductImageUploadResponse(publicBasePath + "/" + filename);
    }

    /**
     * Returns true if the file at the given path starts with the WebP magic bytes
     * ({@code RIFF....WEBP} — bytes 0-3 and 8-11).
     *
     * @param path path to the stored file
     * @return true if the file content matches the WebP signature
     * @throws IOException if the file cannot be read
     */
    private boolean hasWebpMagicBytes(Path path) throws IOException {
        byte[] header = new byte[12];
        try (InputStream in = Files.newInputStream(path)) {
            if (in.read(header) < 12) return false;
        }
        return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }

    /**
     * Returns the file extension for a given MIME type.
     *
     * @param contentType the MIME type
     * @return the file extension including the dot
     */
    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png"  -> ".png";
            case "image/gif"  -> ".gif";
            case "image/webp" -> ".webp";
            default           -> ".jpg";
        };
    }
}
