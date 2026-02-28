package com.urlshortener.transformer;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.model.response.ShortenUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UrlMappingTransformerTest {

    private UrlMappingTransformer transformer;

    @BeforeEach
    void setUp() throws Exception {
        transformer = new UrlMappingTransformer();
        // Inject baseUrl via reflection since @Value isn't active in unit tests
        Field baseUrlField = UrlMappingTransformer.class.getDeclaredField("baseUrl");
        baseUrlField.setAccessible(true);
        baseUrlField.set(transformer, "http://localhost:8080");
    }

    @Test
    void testToResponse() {
        UrlMapping entity = new UrlMapping();
        entity.setShortCode("abc1234");
        entity.setOriginalUrl("https://google.com");
        entity.setCreatedAt(LocalDateTime.of(2026, 2, 28, 12, 0));

        ShortenUrlResponse response = transformer.toResponse(entity);

        assertThat(response.shortCode()).isEqualTo("abc1234");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/abc1234");
        assertThat(response.originalUrl()).isEqualTo("https://google.com");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 2, 28, 12, 0));
    }

    @Test
    void testToEntity() {
        UrlMapping entity = transformer.toEntity("https://example.com", "xyz7890");

        assertThat(entity.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(entity.getShortCode()).isEqualTo("xyz7890");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull(); // Set by @PrePersist
    }

    @Test
    void testToResponseBuildsCorrectShortUrl() {
        UrlMapping entity = new UrlMapping();
        entity.setShortCode("t3sT99z");
        entity.setOriginalUrl("https://example.org/long/path?q=1");
        entity.setCreatedAt(LocalDateTime.now());

        ShortenUrlResponse response = transformer.toResponse(entity);

        assertThat(response.shortUrl()).startsWith("http://localhost:8080/");
        assertThat(response.shortUrl()).endsWith("t3sT99z");
    }
}
