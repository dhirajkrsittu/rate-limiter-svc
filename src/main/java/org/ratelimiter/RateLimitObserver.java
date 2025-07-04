package org.ratelimiter;

public interface RateLimitObserver {
    void onRateLimitExceeded(String userId);
}
