package com.shop.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves uploaded images (announcements and product photos) as static resources. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String announcementUploadDir;
    private final String announcementBasePath;
    private final String productUploadDir;
    private final String productBasePath;

    /**
     * Constructs the configuration with upload directories and public base paths for each image type.
     *
     * @param announcementUploadDir  filesystem directory for announcement images
     * @param announcementBasePath   public URL prefix for announcement images
     * @param productUploadDir       filesystem directory for product images
     * @param productBasePath        public URL prefix for product images
     */
    public WebMvcConfig(
            @Value("${app.upload-dir:uploads/announcements}") String announcementUploadDir,
            @Value("${app.upload-base-url:/uploads/announcements}") String announcementBasePath,
            @Value("${app.product-upload-dir:uploads/products}") String productUploadDir,
            @Value("${app.product-upload-base-url:/uploads/products}") String productBasePath) {
        this.announcementUploadDir = announcementUploadDir;
        this.announcementBasePath = announcementBasePath;
        this.productUploadDir = productUploadDir;
        this.productBasePath = productBasePath;
    }

    /**
     * Registers resource handlers for announcement images (US-ANN-01) and product images (US-CAT-09).
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler(announcementBasePath + "/**")
            .addResourceLocations("file:" + announcementUploadDir + "/");
        registry
            .addResourceHandler(productBasePath + "/**")
            .addResourceLocations("file:" + productUploadDir + "/");
    }
}
