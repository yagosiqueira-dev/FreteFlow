package br.com.freteflow.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MINUTES = 15;

    private record AttemptInfo(int attempts, Instant lastAttempt) {}

    private final ConcurrentHashMap<String, AttemptInfo> attemptsByEmail = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        String key = email.toLowerCase();
        attemptsByEmail.compute(key, (k, current) -> {
            if (current == null || isExpired(current)) {
                return new AttemptInfo(1, Instant.now());
            }
            return new AttemptInfo(current.attempts() + 1, Instant.now());
        });
    }

    public void loginSucceeded(String email) {
        attemptsByEmail.remove(email.toLowerCase());
    }

    public boolean isBlocked(String email) {
        String key = email.toLowerCase();
        AttemptInfo info = attemptsByEmail.get(key);
        if (info == null) return false;

        if (isExpired(info)) {
            attemptsByEmail.remove(key);
            return false;
        }

        return info.attempts() >= MAX_ATTEMPTS;
    }

    private boolean isExpired(AttemptInfo info) {
        return Instant.now().isAfter(info.lastAttempt().plus(BLOCK_DURATION_MINUTES, ChronoUnit.MINUTES));
    }
}