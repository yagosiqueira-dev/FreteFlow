package br.com.freteflow.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MINUTES = 15;
    static final int MAX_TRACKED_EMAILS = 10_000;
    private static final long CLEANUP_INTERVAL_MILLIS = 60_000L;
    private static final long CLEANUP_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(CLEANUP_INTERVAL_MILLIS);

    private record AttemptInfo(int attempts, Instant lastAttempt) {}

    private final ConcurrentMap<String, AttemptInfo> attemptsByEmail = new ConcurrentHashMap<>();
    private final Semaphore entrySlots = new Semaphore(MAX_TRACKED_EMAILS);
    private final Clock clock;
    private final LongSupplier nanoTime;
    private long lastCapacityCleanupNanos;
    private boolean capacityCleanupPerformed;

    public LoginAttemptService() {
        this(Clock.systemUTC(), System::nanoTime);
    }

    LoginAttemptService(Clock clock) {
        this(clock, System::nanoTime);
    }

    LoginAttemptService(Clock clock, LongSupplier nanoTime) {
        this.clock = clock;
        this.nanoTime = nanoTime;
    }

    public void loginFailed(String email) {
        String key = normalize(email);
        while (true) {
            AttemptInfo current = attemptsByEmail.get(key);
            if (current == null) {
                if (!tryAcquireEntrySlot()) {
                    return;
                }
                if (attemptsByEmail.putIfAbsent(key, new AttemptInfo(1, clock.instant())) == null) {
                    return;
                }
                entrySlots.release();
                continue;
            }

            Instant now = clock.instant();
            AttemptInfo updated = isExpired(current, now)
                    ? new AttemptInfo(1, now)
                    : new AttemptInfo(Math.min(MAX_ATTEMPTS, current.attempts() + 1), now);
            if (attemptsByEmail.replace(key, current, updated)) {
                return;
            }
        }
    }

    public void loginSucceeded(String email) {
        removeEntry(normalize(email));
    }

    public boolean isBlocked(String email) {
        String key = normalize(email);
        while (true) {
            Instant now = clock.instant();
            AttemptInfo info = attemptsByEmail.get(key);
            if (info != null) {
                if (isExpired(info, now)) {
                    if (attemptsByEmail.replace(key, info, new AttemptInfo(0, now))) {
                        return false;
                    }
                    continue;
                }
                return info.attempts() >= MAX_ATTEMPTS;
            }

            if (!tryAcquireEntrySlot()) {
                return true;
            }
            if (attemptsByEmail.putIfAbsent(key, new AttemptInfo(0, clock.instant())) == null) {
                return false;
            }
            entrySlots.release();
        }
    }

    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MILLIS)
    public void cleanupExpiredAttempts() {
        removeExpiredAttempts(clock.instant());
    }

    int removeExpiredAttempts(Instant now) {
        int removed = 0;
        for (var entry : attemptsByEmail.entrySet()) {
            if (isExpired(entry.getValue(), now) && attemptsByEmail.remove(entry.getKey(), entry.getValue())) {
                entrySlots.release();
                removed++;
            }
        }
        return removed;
    }

    private boolean tryAcquireEntrySlot() {
        if (entrySlots.tryAcquire()) {
            return true;
        }

        cleanupExpiredEntriesWhenAtCapacity();
        return entrySlots.tryAcquire();
    }

    /**
     * Capacity pressure may trigger a global sweep, but at most once per cleanup interval.
     * Synchronization is only contended while the map is full and ensures callers waiting for
     * an in-progress sweep retry acquisition after that sweep has released any expired slots.
     */
    private synchronized void cleanupExpiredEntriesWhenAtCapacity() {
        long currentNanos = nanoTime.getAsLong();
        if (capacityCleanupPerformed
                && currentNanos - lastCapacityCleanupNanos < CLEANUP_INTERVAL_NANOS) {
            return;
        }

        capacityCleanupPerformed = true;
        lastCapacityCleanupNanos = currentNanos;
        removeExpiredAttempts(clock.instant());
    }

    public void loginAborted(String email) {
        String key = normalize(email);
        while (true) {
            AttemptInfo current = attemptsByEmail.get(key);
            if (current == null || current.attempts() != 0) {
                return;
            }
            if (attemptsByEmail.remove(key, current)) {
                entrySlots.release();
                return;
            }
        }
    }

    private void removeEntry(String key) {
        if (attemptsByEmail.remove(key) != null) {
            entrySlots.release();
        }
    }

    private String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private boolean isExpired(AttemptInfo info, Instant now) {
        return now.isAfter(info.lastAttempt().plus(BLOCK_DURATION_MINUTES, ChronoUnit.MINUTES));
    }
}
