package com.urlshortener.model.response;

public record ErrorResponse(
        String error,
        String message,
        int status) {
}
