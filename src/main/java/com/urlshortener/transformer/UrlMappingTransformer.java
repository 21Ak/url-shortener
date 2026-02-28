package com.urlshortener.transformer;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.model.response.ShortenUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Transforms between UrlMapping entity and response DTOs.
 */
@Component
public class UrlMappingTransformer {

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Converts a UrlMapping entity to a ShortenUrlResponse DTO.
     */
    public ShortenUrlResponse toResponse(UrlMapping entity) {
        return new ShortenUrlResponse(
                entity.getShortCode(),
                baseUrl + "/" + entity.getShortCode(),
                entity.getOriginalUrl(),
                entity.getCreatedAt());
    }

    /**
     * Creates a new UrlMapping entity from the original URL and generated short
     * code.
     */
    public UrlMapping toEntity(String originalUrl, String shortCode) {
        UrlMapping entity = new UrlMapping();
            entity.setOriginalUrl(originalUrl);
        entity.setShortCode(shortCode);
        return entity;
    }
}
