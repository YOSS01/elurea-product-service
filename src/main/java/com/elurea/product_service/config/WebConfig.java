package com.elurea.product_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

/**
 * Serves uploaded images from the local storage directory under /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String location;

    public WebConfig(AppProperties properties) {
        this.location = Paths.get(properties.storage().localDir()).toAbsolutePath().normalize().toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // File names are random UUIDs and never overwritten, so browsers may cache them for a long time.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
    }
}
