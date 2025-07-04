package org.ratelimiter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FixedWindowStrategy implements RateLimitingStrategy {
    private final int windowSize;
    private final Map<String, UserWindow> userWindows = new HashMap<>();
    private final ScheduledExecutorService scheduler;

    public FixedWindowStrategy(int windowSize) {
        this.windowSize = windowSize;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::resetWindow, 1, 1, TimeUnit.SECONDS);
    }

    private static class UserWindow {
        int requestCount;
        long lastResetTime;

        UserWindow() {
            this.requestCount = 0;
            this.lastResetTime = System.nanoTime();
        }
    }

    @Override
    public synchronized boolean isAllowed(String userId) {
        UserWindow window = userWindows.getOrDefault(userId, new UserWindow());
        long currentTime = System.nanoTime();
        long timeElapsed = currentTime - window.lastResetTime;

        if (timeElapsed > TimeUnit.SECONDS.toNanos(windowSize)) {
            window.requestCount = 0; // Reset count after each window
        }

        if (window.requestCount < windowSize) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized void consumeToken(String userId) {
        UserWindow window = userWindows.getOrDefault(userId, new UserWindow());
        window.requestCount++;
        userWindows.put(userId, window);
    }

    private void resetWindow() {
        long currentTime = System.nanoTime();
        for (UserWindow window : userWindows.values()) {
            long timeElapsed = currentTime - window.lastResetTime;
            if (timeElapsed > TimeUnit.SECONDS.toNanos(windowSize)) {
                window.requestCount = 0;
            }
        }
    }
}
