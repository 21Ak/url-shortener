package com.urlshortener.helper;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HashBasedShortCodeGeneratorTest {

    private final HashBasedShortCodeGenerator generator = new HashBasedShortCodeGenerator();

    @Test
    void testGeneratedCodeIsSevenCharacters() {
        String code = generator.generate("https://google.com");
        assertThat(code).hasSize(7);
    }

    @Test
    void testGeneratedCodeIsBase62() {
        String code = generator.generate("https://example.com");
        assertThat(code).matches("[0-9A-Za-z]{7}");
    }

    @Test
    void testSameUrlProducesDifferentCodes() {
        // Due to UUID salt, same URL should produce different codes
        String code1 = generator.generate("https://google.com");
        String code2 = generator.generate("https://google.com");
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void testUniquenessAcrossMultipleCalls() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate("https://test.com/" + i));
        }
        // All 1000 codes should be unique (collision probability is negligible)
        assertThat(codes).hasSize(1000);
    }
}
