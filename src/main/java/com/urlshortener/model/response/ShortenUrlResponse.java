package com.urlshortener.model.response;

import java.time.LocalDateTime;

public record ShortenUrlResponse(
        String shortCode,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt) {
}
