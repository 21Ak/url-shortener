package com.urlshortener.exception;

public class ShortCodeCollisionException extends RuntimeException {

    public ShortCodeCollisionException(String url) {
        super("Failed to generate unique short code for URL: " + url);
    }
}
