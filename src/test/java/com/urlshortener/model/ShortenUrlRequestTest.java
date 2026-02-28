package com.urlshortener.model;

import com.urlshortener.model.request.ShortenUrlRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShortenUrlRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUrl_NoViolations() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://google.com");
        Set<ConstraintViolation<ShortenUrlRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testBlankUrl_HasViolation() {
        ShortenUrlRequest request = new ShortenUrlRequest("");
        Set<ConstraintViolation<ShortenUrlRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("URL cannot be empty");
    }

    @Test
    void testNullUrl_HasViolation() {
        ShortenUrlRequest request = new ShortenUrlRequest(null);
        Set<ConstraintViolation<ShortenUrlRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("URL cannot be empty");
    }
}
