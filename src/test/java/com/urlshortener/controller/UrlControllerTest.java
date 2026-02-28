package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.request.ShortenUrlRequest;
import com.urlshortener.model.response.ShortenUrlResponse;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testShortenUrl_Returns201() throws Exception {
        ShortenUrlResponse response = new ShortenUrlResponse(
                "abc1234", "http://localhost:8080/abc1234", "https://google.com", LocalDateTime.now());
        when(urlService.shortenUrl(any(ShortenUrlRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://google.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"))
                .andExpect(jsonPath("$.originalUrl").value("https://google.com"));
    }

    @Test
    void testShortenUrl_BlankUrl_Returns400() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testShortenUrl_InvalidUrl_Returns400() throws Exception {
        when(urlService.shortenUrl(any(ShortenUrlRequest.class)))
                .thenThrow(new InvalidUrlException("not-a-url"));

        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"not-a-url\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRedirect_Returns302() throws Exception {
        when(urlService.resolveUrl("abc1234")).thenReturn("https://google.com");

        mockMvc.perform(get("/abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    void testRedirect_NotFound_Returns404() throws Exception {
        when(urlService.resolveUrl("nope123")).thenThrow(new UrlNotFoundException("nope123"));

        mockMvc.perform(get("/nope123"))
                .andExpect(status().isNotFound());
    }
}
