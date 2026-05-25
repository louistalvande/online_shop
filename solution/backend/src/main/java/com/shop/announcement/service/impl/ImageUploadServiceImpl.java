package com.shop.announcement.service.impl;

import com.shop.announcement.dto.ImageUploadResponse;
import com.shop.announcement.entity.ImageOrientation;
import com.shop.announcement.exception.UnsupportedImageTypeException;
import com.shop.announcement.service.ImageUploadService;
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

/** {@link ImageUploadService} implementation — stores images on the local filesystem. */
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final Path uploadDir;
    private final String publicBasePath;

    /**
     * Constructs the service and ensures the upload directory exists.
     *
     * @param uploadDirPath  filesystem path to the upload directory (configurable via app.upload-dir)
     * @param publicBasePath public URL base path for stored images (configurable via app.upload-base-url)
     */
    public ImageUploadServiceImpl(
            @Value("${app.upload-dir:uploads/announcements}") String uploadDirPath,
            @Value("${app.upload-base-url:/uploads/announcements}") String publicBasePath) {
        this.uploadDir = Paths.get(uploadDirPath);
        this.publicBasePath = publicBasePath;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload directory: " + uploadDirPath, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ImageUploadResponse store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new UnsupportedImageTypeException(contentType);
        }

        String extension = extensionFor(contentType);
        String filename = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store image: " + filename, e);
        }

        ImageOrientation orientation = detectOrientation(destination);
        String imageUrl = publicBasePath + "/" + filename;
        return new ImageUploadResponse(imageUrl, orientation);
    }

    /**
     * Reads the image dimensions and returns LANDSCAPE if width &gt; height, PORTRAIT otherwise.
     *
     * @param path path to the stored image file
     * @return the detected orientation
     */
    private ImageOrientation detectOrientation(Path path) {
        try {
            BufferedImage img = ImageIO.read(path.toFile());
            if (img == null) return ImageOrientation.LANDSCAPE;
            return img.getWidth() > img.getHeight()
                    ? ImageOrientation.LANDSCAPE
                    : ImageOrientation.PORTRAIT;
        } catch (IOException e) {
            return ImageOrientation.LANDSCAPE;
        }
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
