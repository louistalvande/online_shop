package com.shop.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves uploaded images (announcements, product photos, vendor logo and banner) as static resources. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String announcementUploadDir;
    private final String announcementBasePath;
    private final String productUploadDir;
    private final String productBasePath;
    private final String logoUploadDir;
    private final String logoBasePath;
    private final String bannerUploadDir;
    private final String bannerBasePath;

    /**
     * Constructs the configuration with upload directories and public base paths for each image type.
     *
     * @param announcementUploadDir filesystem directory for announcement images
     * @param announcementBasePath  public URL prefix for announcement images
     * @param productUploadDir      filesystem directory for product images
     * @param productBasePath       public URL prefix for product images
     * @param logoUploadDir         filesystem directory for the vendor logo
     * @param logoBasePath          public URL prefix for the vendor logo
     * @param bannerUploadDir       filesystem directory for the vendor banner
     * @param bannerBasePath        public URL prefix for the vendor banner
     */
    public WebMvcConfig(
            @Value("${app.upload-dir:uploads/announcements}") String announcementUploadDir,
            @Value("${app.upload-base-url:/uploads/announcements}") String announcementBasePath,
            @Value("${app.product-upload-dir:uploads/products}") String productUploadDir,
            @Value("${app.product-upload-base-url:/uploads/products}") String productBasePath,
            @Value("${app.vendor-logo-upload-dir:uploads/logos}") String logoUploadDir,
            @Value("${app.vendor-logo-upload-base-url:/uploads/logos}") String logoBasePath,
            @Value("${app.vendor-banner-upload-dir:uploads/banners}") String bannerUploadDir,
            @Value("${app.vendor-banner-upload-base-url:/uploads/banners}") String bannerBasePath) {
        this.announcementUploadDir = announcementUploadDir;
        this.announcementBasePath = announcementBasePath;
        this.productUploadDir = productUploadDir;
        this.productBasePath = productBasePath;
        this.logoUploadDir = logoUploadDir;
        this.logoBasePath = logoBasePath;
        this.bannerUploadDir = bannerUploadDir;
        this.bannerBasePath = bannerBasePath;
    }

    /**
     * Registers resource handlers for all uploaded image types:
     * announcement images (US-ANN-01), product images (US-CAT-09),
     * vendor logo and vendor banner (BES-VND-015).
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
        registry
            .addResourceHandler(logoBasePath + "/**")
            .addResourceLocations("file:" + logoUploadDir + "/");
        registry
            .addResourceHandler(bannerBasePath + "/**")
            .addResourceLocations("file:" + bannerUploadDir + "/");
    }
}
