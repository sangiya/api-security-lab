package com.sangiya.apisec.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangiya.apisec.error.GlobalExceptionHandler;

class RateLimitFilterTest {

    private static final long MAX_REQUESTS = 3;
    private static final long WINDOW_MS = 60_000;
    private static final String PROTECTED = "/api/auth/login";

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        RateLimitFilter filter = new RateLimitFilter(MAX_REQUESTS, WINDOW_MS, PROTECTED,
                objectMapper, exceptionHandler);

        this.mvc = MockMvcBuilders
                .standaloneSetup(new PassthroughController())
                .addFilters(filter)
                .build();
    }

    @Test
    void withinLimit_requestsAreAllowed() throws Exception {
        // Requests from the same IP (remote address defaults to 127.0.0.1)
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/api/auth/login"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void exceedingLimit_returns429() throws Exception {
        // The 4th request from the same IP within the window is rejected
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Too many requests. Try again later."));
    }

    @Test
    void unProtectedPath_isNotRateLimited() throws Exception {
        // 10 requests to a non-protected path are all allowed
        for (int i = 0; i < 10; i++) {
            mvc.perform(get("/api/health"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void forwardedForHeader_ipIsolated() throws Exception {
        // One client IP exhausts its own window...
        mvc.perform(get("/api/auth/login").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/login").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isTooManyRequests());

        // ...while a different client IP is unaffected
        mvc.perform(get("/api/auth/login").header("X-Forwarded-For", "10.0.0.2"))
                .andExpect(status().isOk());
    }
}
