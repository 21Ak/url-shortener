package com.urlshortener.service;

import com.urlshortener.model.request.ShortenUrlRequest;
import com.urlshortener.model.response.ShortenUrlResponse;

public interface UrlService {

    /**
     * Shorten a URL. Validates the URL, generates a unique short code, persists it.
     *
     * @param request the shorten URL request containing the original URL
     * @return response containing the short code and full short URL
     */
    ShortenUrlResponse shortenUrl(ShortenUrlRequest request);

    /**
     * Resolve a short code to the original URL.
     *
     * @param shortCode the short code to look up
     * @return the original URL
     */
    String resolveUrl(String shortCode);
}
