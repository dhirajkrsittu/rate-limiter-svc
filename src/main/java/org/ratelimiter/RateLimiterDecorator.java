package org.ratelimiter;

import java.util.*;

public class RateLimiterDecorator implements RateLimitingStrategy {
    private final RateLimitingStrategy strategy;
    private final List<RateLimitObserver> observers = new ArrayList<>();

    public RateLimiterDecorator(RateLimitingStrategy strategy) {
        this.strategy = strategy;
    }

    public void addObserver(RateLimitObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String userId) {
        for (RateLimitObserver observer : observers) {
            observer.onRateLimitExceeded(userId);
        }
    }

    @Override
    public boolean isAllowed(String userId) {
        boolean allowed = strategy.isAllowed(userId);
        if (!allowed) {
            notifyObservers(userId);
        }
        return allowed;
    }

    @Override
    public void consumeToken(String userId) {
        strategy.consumeToken(userId);
    }
}
