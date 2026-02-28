package com.urlshortener.helper;

import com.urlshortener.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validates that a given string is a well-formed HTTP(S) URL.
 * Throws InvalidUrlException if validation fails.
 */
@Component
public class UrlValidator {

    /**
     * Validates the given URL string. Throws InvalidUrlException if invalid.
     *
     * @param url the URL to validate
     * @throws InvalidUrlException if the URL is null, blank, or not a valid HTTP(S)
     *                             URL
     */
    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException(url);
        }

        try {
            URI uri = new URI(url);
            uri.toURL(); // triggers MalformedURLException for bad schemes

            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new InvalidUrlException(url);
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new InvalidUrlException(url);
            }
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            throw new InvalidUrlException(url);
        }
    }
}
