package com.acme.salary.common;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * CORS for the dev frontend, plus optional static hosting of a built SPA:
 * files from classpath:/static/ or ./static/, and index.html for unknown non-API routes.
 * Nothing is served (and nothing breaks) when those folders are absent.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] RESERVED_PREFIXES = {"api/", "v3/", "swagger-ui"};

    private final String[] allowedOrigins;
    private final String[] staticLocations;
    private final boolean spaFallback;

    public WebConfig(@Value("${acme.cors.allowed-origins:}") String[] allowedOrigins,
                     @Value("${acme.static.locations:classpath:/static/}") String[] staticLocations,
                     @Value("${acme.static.spa-fallback:true}") boolean spaFallback) {
        this.allowedOrigins = allowedOrigins;
        this.staticLocations = staticLocations;
        this.spaFallback = spaFallback;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.length == 0 || allowedOrigins[0].isBlank()) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(staticLocations)
                .resourceChain(false)
                .addResolver(new SpaResolver());
    }

    private final class SpaResolver extends PathResourceResolver {
        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable() && !resourcePath.isEmpty()) {
                return requested;
            }
            if (!spaFallback || isReserved(resourcePath) || resourcePath.contains(".")) {
                return null;
            }
            Resource index = location.createRelative("index.html");
            return index.exists() && index.isReadable() ? index : null;
        }

        private boolean isReserved(String path) {
            for (String prefix : RESERVED_PREFIXES) {
                if (path.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
    }
}
