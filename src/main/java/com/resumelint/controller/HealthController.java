package com.resumelint.controller;

import com.resumelint.dto.HealthStatusDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Equivalent of routes/health.ts. Public, no auth required. */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/healthz")
    public HealthStatusDto healthCheck() {
        return new HealthStatusDto("ok");
    }
}
