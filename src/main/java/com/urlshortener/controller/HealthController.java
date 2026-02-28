package com.urlshortener.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @GetMapping
    public ResponseEntity<String> health() {
        log.info("Health check");
        return ResponseEntity.ok("OK");
    }
}
