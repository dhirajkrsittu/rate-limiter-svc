package org.ratelimiter;

public class RateLimiterApp {
    public static void main(String[] args) throws InterruptedException {
        // Strategy: Using TokenBucketStrategy
        RateLimitingStrategy tokenBucketStrategy = new TokenBucketStrategy(5, 1);

        // Decorator: Adding logging functionality to the rate limiter
        RateLimiterDecorator rateLimiterWithLogging = new RateLimiterDecorator(tokenBucketStrategy);
        rateLimiterWithLogging.addObserver(new RateLimitLogger());

        // Singleton: Get the single instance of the RateLimiter
        RateLimiterSingleton rateLimiter = RateLimiterSingleton.getInstance(rateLimiterWithLogging);

        String userId = "user1";

        // Simulating requests
        for (int i = 0; i < 10; i++) {
            if (rateLimiter.isAllowed(userId)) {
                System.out.println("Request " + (i + 1) + " allowed.");
                rateLimiter.consumeToken(userId);
            } else {
                System.out.println("Request " + (i + 1) + " denied.");
            }
            Thread.sleep(200);  // Simulate delay between requests
        }
    }
}
