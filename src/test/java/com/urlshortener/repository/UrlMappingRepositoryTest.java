package com.urlshortener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.urlshortener.entity.UrlMapping;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class UrlMappingRepositoryTest {

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void testFindByShortCode_Found() {
        // Arrange
        UrlMapping entity = new UrlMapping();
        entity.setShortCode("abc1234");
        entity.setOriginalUrl("https://google.com");
        repository.save(entity);

        // Act
        Optional<UrlMapping> result = repository.findByShortCode("abc1234");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getOriginalUrl()).isEqualTo("https://google.com");
        assertThat(result.get().getShortCode()).isEqualTo("abc1234");
        assertThat(result.get().getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByShortCode_NotFound() {
        // Act
        Optional<UrlMapping> result = repository.findByShortCode("nonexistent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testExistsByShortCode() {
        // Arrange
        UrlMapping entity = new UrlMapping();
        entity.setShortCode("test123");
        entity.setOriginalUrl("https://example.com");
        repository.save(entity);

        // Act & Assert
        assertThat(repository.existsByShortCode("test123")).isTrue();
        assertThat(repository.existsByShortCode("nope456")).isFalse();
    }
}
