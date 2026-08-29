package com.sangiya.apisec.ratelimit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal controller used by {@link RateLimitFilterTest} so the rate-limiting
 * filter can be exercised against real requests without pulling in the full
 * application context. The login path is a protected (rate-limited) route; the
 * health path is not, so the test can prove the filter only applies where
 * configured. Registered with standalone MockMvc.
 */
@RestController
class PassthroughController {

    @GetMapping("/api/auth/login")
    public String login() {
        return "ok";
    }

    @GetMapping("/api/health")
    public String health() {
        return "ok";
    }
}
