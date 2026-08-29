package com.sangiya.apisec.ratelimit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Fixed-window rate limiter for a single key (e.g. one client IP).
 * Tracks the timestamp at which the current window started and the number
 * of requests consumed inside it. Windows are reset lazily on first request
 * after the window has elapsed, so no background janitor thread is needed.
 */
public class RateLimiter {

    private final long maxRequests;
    private final long windowMs;

    private final AtomicLong windowStart;
    private final AtomicLong count;

    public RateLimiter(long maxRequests, long windowMs) {
        if (maxRequests <= 0 || windowMs <= 0) {
            throw new IllegalArgumentException("maxRequests and windowMs must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        long now = System.currentTimeMillis();
        this.windowStart = new AtomicLong(now);
        this.count = new AtomicLong(0);
    }

    /**
     * Attempts to consume one request. Returns true if the request is allowed
     * and false if the client has exceeded the configured limit for the window.
     */
    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long start = windowStart.get();
        long elapsed = now - start;
        if (elapsed >= windowMs) {
            // Window expired: race to open a new window.
            if (windowStart.compareAndSet(start, now)) {
                count.set(0);
            }
        }
        return count.incrementAndGet() <= maxRequests;
    }

    long getCurrentCount() {
        return count.get();
    }
}
