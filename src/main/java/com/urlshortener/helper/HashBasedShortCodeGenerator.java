package com.urlshortener.helper;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Generates a 7-character short code using SHA-256 hash → Base62 encoding.
 * Appends a random salt (UUID) to avoid deterministic collisions for the same
 * URL.
 */
@Component
public class HashBasedShortCodeGenerator implements ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 7;

    @Override
    public String generate(String url) {
        String input = url + UUID.randomUUID();
        byte[] hash = sha256(input);
        return toBase62(hash, SHORT_CODE_LENGTH);
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String toBase62(byte[] hash, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // Use unsigned byte value to index into BASE62_CHARS
            int index = Byte.toUnsignedInt(hash[i]) % BASE62_CHARS.length();
            sb.append(BASE62_CHARS.charAt(index));
        }
        return sb.toString();
    }
}
