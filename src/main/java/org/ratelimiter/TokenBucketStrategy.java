package org.ratelimiter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenBucketStrategy implements RateLimitingStrategy {
    private final int capacity;
    private final int refillRate;
    private final Map<String, UserBucket> userBuckets = new HashMap<>();
    private final ScheduledExecutorService scheduler;

    public TokenBucketStrategy(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refillTokens, 1, 1, TimeUnit.SECONDS);
    }

    private static class UserBucket {
        int tokens;
        long lastRefillTime;

        UserBucket(int capacity) {
            this.tokens = capacity;
            this.lastRefillTime = System.nanoTime();
        }
    }

    @Override
    public synchronized boolean isAllowed(String userId) {
        UserBucket bucket = userBuckets.getOrDefault(userId, new UserBucket(capacity));
        long currentTime = System.nanoTime();
        long timeElapsed = currentTime - bucket.lastRefillTime;
        int tokensToAdd = (int) (timeElapsed / TimeUnit.SECONDS.toNanos(1)) * refillRate;
        bucket.tokens = Math.min(bucket.tokens + tokensToAdd, capacity);
        bucket.lastRefillTime = currentTime;
        return bucket.tokens > 0;
    }

    @Override
    public synchronized void consumeToken(String userId) {
        UserBucket bucket = userBuckets.getOrDefault(userId, new UserBucket(capacity));
        if (bucket.tokens > 0) {
            bucket.tokens--;
        }
    }

    private void refillTokens() {
        for (UserBucket bucket : userBuckets.values()) {
            long timeElapsed = System.nanoTime() - bucket.lastRefillTime;
            int tokensToAdd = (int) (timeElapsed / TimeUnit.SECONDS.toNanos(1)) * refillRate;
            bucket.tokens = Math.min(bucket.tokens + tokensToAdd, capacity);
            bucket.lastRefillTime = System.nanoTime();
        }
    }
}
