package org.ratelimiter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeakyBucketStrategy implements RateLimitingStrategy {
    private final int capacity;
    private final int leakRate;
    private final Map<String, UserBucket> userBuckets = new HashMap<>();
    private final ScheduledExecutorService scheduler;

    public LeakyBucketStrategy(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::leakTokens, 1, 1, TimeUnit.SECONDS);
    }

    private static class UserBucket {
        int water;  // Represents the current amount of water in the bucket
        long lastLeakTime;

        UserBucket(int capacity) {
            this.water = 0;
            this.lastLeakTime = System.nanoTime();
        }
    }

    @Override
    public synchronized boolean isAllowed(String userId) {
        UserBucket bucket = userBuckets.getOrDefault(userId, new UserBucket(capacity));
        long currentTime = System.nanoTime();
        long timeElapsed = currentTime - bucket.lastLeakTime;
        int waterToLeak = (int) (timeElapsed / TimeUnit.SECONDS.toNanos(1)) * leakRate;
        bucket.water = Math.max(0, bucket.water - waterToLeak); // Leak water
        bucket.lastLeakTime = currentTime;
        return bucket.water < capacity; // If the bucket isn't full, allow the request
    }

    @Override
    public synchronized void consumeToken(String userId) {
        UserBucket bucket = userBuckets.getOrDefault(userId, new UserBucket(capacity));
        if (bucket.water < capacity) {
            bucket.water++;
        }
    }

    private void leakTokens() {
        for (UserBucket bucket : userBuckets.values()) {
            long timeElapsed = System.nanoTime() - bucket.lastLeakTime;
            int waterToLeak = (int) (timeElapsed / TimeUnit.SECONDS.toNanos(1)) * leakRate;
            bucket.water = Math.max(0, bucket.water - waterToLeak);
            bucket.lastLeakTime = System.nanoTime();
        }
    }
}
