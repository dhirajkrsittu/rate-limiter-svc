package org.ratelimiter;

public class RateLimiterSingleton {
    private static RateLimiterSingleton instance;
    private RateLimitingStrategy strategy;

    private RateLimiterSingleton(RateLimitingStrategy strategy) {
        this.strategy = strategy;
    }

    public static synchronized RateLimiterSingleton getInstance(RateLimitingStrategy strategy) {
        if (instance == null) {
            instance = new RateLimiterSingleton(strategy);
        }
        return instance;
    }

    public boolean isAllowed(String userId) {
        return strategy.isAllowed(userId);
    }

    public void consumeToken(String userId) {
        strategy.consumeToken(userId);
    }
}
