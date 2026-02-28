package com.urlshortener.helper;

import com.urlshortener.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @Test
    void testValidHttpUrl() {
        assertThatCode(() -> validator.validate("http://example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidHttpsUrl() {
        assertThatCode(() -> validator.validate("https://www.google.com/search?q=test"))
                .doesNotThrowAnyException();
    }

    @Test
    void testNullUrl_ThrowsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void testBlankUrl_ThrowsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate("   "))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void testMissingScheme_ThrowsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate("www.google.com"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void testFtpScheme_ThrowsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate("ftp://files.example.com/data"))
                .isInstanceOf(InvalidUrlException.class);
    }
}
