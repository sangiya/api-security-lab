package com.sangiya.apisec.ratelimit;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangiya.apisec.error.ErrorResponse;
import com.sangiya.apisec.error.GlobalExceptionHandler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Fixed-window rate limiter applied per client IP on the configured auth
 * endpoints. Returns HTTP 429 (Too Many Requests) with a JSON body when the
 * limit is exceeded. Limiters are stored in a bounded map keyed by IP.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String MAX_REQUEST_HEADER = "X-Forwarded-For";

    private final long maxRequests;
    private final long windowMs;
    private final Set<String> protectedPaths;
    private final ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final GlobalExceptionHandler exceptionHandler;

    public RateLimitFilter(@Value("${app.ratelimit.max-requests}") long maxRequests,
                           @Value("${app.ratelimit.window-ms}") long windowMs,
                           @Value("${app.ratelimit.protected-paths}") String protectedPaths,
                           ObjectMapper objectMapper,
                           GlobalExceptionHandler exceptionHandler) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.protectedPaths = Set.of(protectedPaths.split(","));
        this.objectMapper = objectMapper;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String stripped = contextPath.isEmpty() ? path : path.substring(contextPath.length());

        if (!protectedPaths.contains(stripped)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        RateLimiter limiter = limiters.computeIfAbsent(clientIp,
                ip -> new RateLimiter(maxRequests, windowMs));

        if (limiter.tryAcquire()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        ErrorResponse body = exceptionHandler.buildRateLimited(path);
        objectMapper.writeValue(response.getWriter(), body);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(MAX_REQUEST_HEADER);
        if (forwarded != null && !forwarded.isBlank()) {
            // take the left-most entry, the original client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    int limiterCount() {
        return limiters.size();
    }
}
