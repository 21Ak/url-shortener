package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.ShortCodeCollisionException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.helper.ShortCodeGenerator;
import com.urlshortener.helper.UrlValidator;
import com.urlshortener.model.request.ShortenUrlRequest;
import com.urlshortener.model.response.ShortenUrlResponse;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.transformer.UrlMappingTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final int MAX_RETRIES = 3;

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlValidator urlValidator;
    private final UrlMappingTransformer transformer;

    @Override
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        String originalUrl = request.url();

        // 1. Validate URL
        urlValidator.validate(originalUrl);

        // 2. Generate short code with retry on collision
        String shortCode = generateUniqueShortCode(originalUrl);

        // 3. Build and persist entity
        UrlMapping entity = transformer.toEntity(originalUrl, shortCode);
        UrlMapping saved = repository.save(entity);

        log.info("Shortened URL: {} → {}", originalUrl, shortCode);

        // 4. Return response DTO
        return transformer.toResponse(saved);
    }

    @Override
    public String resolveUrl(String shortCode) {
        UrlMapping entity = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        log.info("Resolved short code: {} → {}", shortCode, entity.getOriginalUrl());
        return entity.getOriginalUrl();
    }

    private String generateUniqueShortCode(String url) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String code = shortCodeGenerator.generate(url);
            if (!repository.existsByShortCode(code)) {
                return code;
            }
            log.warn("Short code collision on attempt {} for URL: {}", attempt + 1, url);
        }
        throw new ShortCodeCollisionException(url);
    }
}
