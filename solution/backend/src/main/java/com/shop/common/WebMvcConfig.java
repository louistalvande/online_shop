package com.shop.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves uploaded announcement images as static resources (US-ANN-01). */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String publicBasePath;

    /**
     * Constructs the configuration with the upload directory and public base path.
     *
     * @param uploadDir      filesystem directory where images are stored
     * @param publicBasePath public URL path prefix used to serve them
     */
    public WebMvcConfig(
            @Value("${app.upload-dir:uploads/announcements}") String uploadDir,
            @Value("${app.upload-base-url:/uploads/announcements}") String publicBasePath) {
        this.uploadDir = uploadDir;
        this.publicBasePath = publicBasePath;
    }

    /**
     * Registers a resource handler that maps the public URL path to the upload directory on disk.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler(publicBasePath + "/**")
            .addResourceLocations("file:" + uploadDir + "/");
    }
}
