package org.ratelimiter;

public class RateLimitLogger implements RateLimitObserver {
    @Override
    public void onRateLimitExceeded(String userId) {
        System.out.println("Rate limit exceeded for user: " + userId);
    }
}
