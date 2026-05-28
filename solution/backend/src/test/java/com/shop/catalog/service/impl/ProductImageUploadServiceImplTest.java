package com.shop.catalog.service.impl;

import com.shop.catalog.dto.ProductImageUploadResponse;
import com.shop.catalog.exception.UnsupportedProductImageTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for {@link ProductImageUploadServiceImpl}. */
class ProductImageUploadServiceImplTest {

    @TempDir
    Path tempDir;

    ProductImageUploadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductImageUploadServiceImpl(
                tempDir.toString(),
                "/uploads/products"
        );
    }

    /** Creates a minimal valid 1×1 image encoded as the given format (e.g. "png", "jpeg", "gif"). */
    private byte[] realImageBytes(String format) throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);
        return baos.toByteArray();
    }

    @Test
    void store_jpeg_returnsUrlWithJpgExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", realImageBytes("jpeg"));

        ProductImageUploadResponse response = service.store(file);

        assertThat(response.getImageUrl()).startsWith("/uploads/products/");
        assertThat(response.getImageUrl()).endsWith(".jpg");
    }

    @Test
    void store_png_returnsUrlWithPngExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", realImageBytes("png"));

        ProductImageUploadResponse response = service.store(file);

        assertThat(response.getImageUrl()).startsWith("/uploads/products/");
        assertThat(response.getImageUrl()).endsWith(".png");
    }

    @Test
    void store_gif_returnsUrlWithGifExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "anim.gif", "image/gif", realImageBytes("gif"));

        ProductImageUploadResponse response = service.store(file);

        assertThat(response.getImageUrl()).endsWith(".gif");
    }

    @Test
    void store_webp_returnsUrlWithWebpExtension() {
        // Build minimal WebP header: RIFF + 4-byte size + WEBP + minimal VP8L chunk
        byte[] webp = new byte[30];
        webp[0] = 'R'; webp[1] = 'I'; webp[2] = 'F'; webp[3] = 'F';
        webp[4] = 22;  // file size - 8 (little-endian, only low byte needed for test)
        webp[8] = 'W'; webp[9] = 'E'; webp[10] = 'B'; webp[11] = 'P';
        // VP8L marker so the file is non-empty and passes magic check
        webp[12] = 'V'; webp[13] = 'P'; webp[14] = '8'; webp[15] = 'L';

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.webp", "image/webp", webp);

        ProductImageUploadResponse response = service.store(file);

        assertThat(response.getImageUrl()).endsWith(".webp");
    }

    @Test
    void store_unsupportedType_throwsUnsupportedProductImageTypeException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf-bytes".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(UnsupportedProductImageTypeException.class);
    }

    @Test
    void store_nullContentType_throwsUnsupportedProductImageTypeException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "unknown", null, "bytes".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(UnsupportedProductImageTypeException.class);
    }

    @Test
    void store_fakeImageBytes_withValidMimeType_throwsException() {
        // A file claiming to be JPEG but containing garbage bytes should be rejected on second-pass validation
        MockMultipartFile file = new MockMultipartFile(
                "file", "malicious.jpg", "image/jpeg", "not-an-image".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(UnsupportedProductImageTypeException.class);
    }

    @Test
    void store_writesFileToDisk() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "img.png", "image/png", realImageBytes("png"));

        ProductImageUploadResponse response = service.store(file);

        String filename = response.getImageUrl().substring("/uploads/products/".length());
        assertThat(tempDir.resolve(filename)).exists();
    }
}
