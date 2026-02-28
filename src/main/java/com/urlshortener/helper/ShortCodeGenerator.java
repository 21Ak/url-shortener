package com.urlshortener.helper;

/**
 * Strategy interface for generating short codes from URLs.
 * Allows swapping implementations (hash-based, random, custom alias, etc.)
 */
public interface ShortCodeGenerator {

    /**
     * Generate a short code for the given URL.
     *
     * @param url the original URL
     * @return a short code string (7 characters)
     */
    String generate(String url);
}
