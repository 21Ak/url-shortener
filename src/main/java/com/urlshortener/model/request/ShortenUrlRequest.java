package com.urlshortener.model.request;

import jakarta.validation.constraints.NotBlank;

public record ShortenUrlRequest(
        @NotBlank(message = "URL cannot be empty") String url) {
}
