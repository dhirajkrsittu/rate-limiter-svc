package org.ratelimiter;

public interface RateLimitingStrategy {
    boolean isAllowed(String userId);
    void consumeToken(String userId);
}
