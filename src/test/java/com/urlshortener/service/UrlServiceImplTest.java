package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortCodeCollisionException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.helper.ShortCodeGenerator;
import com.urlshortener.helper.UrlValidator;
import com.urlshortener.model.request.ShortenUrlRequest;
import com.urlshortener.model.response.ShortenUrlResponse;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.transformer.UrlMappingTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private UrlValidator urlValidator;

    @Mock
    private UrlMappingTransformer transformer;

    @InjectMocks
    private UrlServiceImpl urlService;

    // ── shortenUrl tests ──

    @Test
    void testShortenUrl_Success() {
        // Arrange
        ShortenUrlRequest request = new ShortenUrlRequest("https://google.com");
        UrlMapping entity = new UrlMapping();
        entity.setShortCode("abc1234");
        entity.setOriginalUrl("https://google.com");
        entity.setCreatedAt(LocalDateTime.now());

        doNothing().when(urlValidator).validate("https://google.com");
        when(shortCodeGenerator.generate("https://google.com")).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);
        when(transformer.toEntity("https://google.com", "abc1234")).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(transformer.toResponse(entity)).thenReturn(
                new ShortenUrlResponse("abc1234", "http://localhost:8080/abc1234", "https://google.com",
                        entity.getCreatedAt()));

        // Act
        ShortenUrlResponse response = urlService.shortenUrl(request);

        // Assert
        assertThat(response.shortCode()).isEqualTo("abc1234");
        assertThat(response.originalUrl()).isEqualTo("https://google.com");
        verify(repository).save(entity);
    }

    @Test
    void testShortenUrl_InvalidUrl_ThrowsException() {
        ShortenUrlRequest request = new ShortenUrlRequest("not-a-url");
        doThrow(new InvalidUrlException("not-a-url")).when(urlValidator).validate("not-a-url");

        assertThatThrownBy(() -> urlService.shortenUrl(request))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void testShortenUrl_CollisionRetrySuccess() {
        // First call collides, second succeeds
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com");
        UrlMapping entity = new UrlMapping();
        entity.setShortCode("xyz7890");
        entity.setOriginalUrl("https://example.com");
        entity.setCreatedAt(LocalDateTime.now());

        doNothing().when(urlValidator).validate(anyString());
        when(shortCodeGenerator.generate("https://example.com"))
                .thenReturn("COLLIDE")
                .thenReturn("xyz7890");
        when(repository.existsByShortCode("COLLIDE")).thenReturn(true);
        when(repository.existsByShortCode("xyz7890")).thenReturn(false);
        when(transformer.toEntity("https://example.com", "xyz7890")).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(transformer.toResponse(entity)).thenReturn(
                new ShortenUrlResponse("xyz7890", "http://localhost:8080/xyz7890", "https://example.com",
                        entity.getCreatedAt()));

        ShortenUrlResponse response = urlService.shortenUrl(request);

        assertThat(response.shortCode()).isEqualTo("xyz7890");
    }

    @Test
    void testShortenUrl_AllRetriesFail_ThrowsCollisionException() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com");

        doNothing().when(urlValidator).validate(anyString());
        when(shortCodeGenerator.generate("https://example.com")).thenReturn("COLLIDE");
        when(repository.existsByShortCode("COLLIDE")).thenReturn(true);

        assertThatThrownBy(() -> urlService.shortenUrl(request))
                .isInstanceOf(ShortCodeCollisionException.class);
    }

    // ── resolveUrl tests ──

    @Test
    void testResolveUrl_Found() {
        UrlMapping entity = new UrlMapping();
        entity.setOriginalUrl("https://google.com");

        when(repository.findByShortCode("abc1234")).thenReturn(Optional.of(entity));

        String result = urlService.resolveUrl("abc1234");

        assertThat(result).isEqualTo("https://google.com");
    }

    @Test
    void testResolveUrl_NotFound_ThrowsException() {
        when(repository.findByShortCode("nope123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.resolveUrl("nope123"))
                .isInstanceOf(UrlNotFoundException.class);
    }
}
