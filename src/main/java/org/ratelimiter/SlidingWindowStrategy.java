package org.ratelimiter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SlidingWindowStrategy implements RateLimitingStrategy {
    private final int windowSize;
    private final Map<String, LinkedList<Long>> userWindows = new HashMap<>();

    public SlidingWindowStrategy(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public synchronized boolean isAllowed(String userId) {
        LinkedList<Long> window = userWindows.getOrDefault(userId, new LinkedList<>());
        long currentTime = System.nanoTime();

        // Remove requests older than the window size
        window.removeIf(timestamp -> currentTime - timestamp > TimeUnit.SECONDS.toNanos(windowSize));

        // Check if the user is within the rate limit
        if (window.size() < windowSize) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized void consumeToken(String userId) {
        LinkedList<Long> window = userWindows.getOrDefault(userId, new LinkedList<>());
        window.add(System.nanoTime()); // Record the time of the request
        userWindows.put(userId, window);
    }
}
