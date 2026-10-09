package br.com.freteflow.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    @Test
    void blocksAfterFiveFailedAttemptsAndSuccessClearsTheBlock() {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());

        assertThat(service.isBlocked("person@example.com")).isFalse();
        for (int attempt = 1; attempt <= 5; attempt++) {
            service.loginFailed("person@example.com");
        }

        assertThat(service.isBlocked("PERSON@example.com")).isTrue();
        service.loginSucceeded("person@example.com");
        assertThat(service.isBlocked("person@example.com")).isFalse();
    }

    @Test
    void releasesAnEmailAfterTheFifteenMinuteWindow() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);

        for (int attempt = 0; attempt < 5; attempt++) {
            service.loginFailed("expired@example.com");
        }
        assertThat(service.isBlocked("expired@example.com")).isTrue();

        clock.advance(Duration.ofMinutes(15));
        assertThat(service.isBlocked("expired@example.com")).isTrue();

        clock.advance(Duration.ofNanos(1));

        assertThat(service.isBlocked("expired@example.com")).isFalse();
    }

    @Test
    void cleanupRemovesExpiredEntriesAcrossEmails() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);
        service.loginFailed("stale-one@example.com");
        service.loginFailed("stale-two@example.com");

        clock.advance(Duration.ofMinutes(15).plusNanos(1));
        service.loginFailed("active@example.com");

        assertThat(service.removeExpiredAttempts(clock.instant())).isEqualTo(2);
        assertThat(service.isBlocked("unrelated@example.com")).isFalse();
    }

    @Test
    void capsTrackedEmailsAndFailsClosedUntilExpiredEntriesAreCleaned() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);

        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS; index++) {
            assertThat(service.isBlocked("user-" + index + "@example.com")).isFalse();
        }
        assertThat(service.isBlocked("overflow@example.com")).isTrue();

        clock.advance(Duration.ofMinutes(15).plusNanos(1));
        service.cleanupExpiredAttempts();
        assertThat(service.isBlocked("overflow@example.com")).isFalse();
    }

    @Test
    void fullCapacitySweepImmediatelyReclaimsExpiredEntriesAndPreservesActiveOnes() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);

        assertThat(service.isBlocked("expired@example.com")).isFalse();
        clock.advance(Duration.ofMinutes(15).plusNanos(1));

        for (int attempt = 0; attempt < 5; attempt++) {
            service.loginFailed("active-blocked@example.com");
        }
        assertThat(service.isBlocked("active-blocked@example.com")).isTrue();

        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 2; index++) {
            assertThat(service.isBlocked("active-" + index + "@example.com")).isFalse();
        }

        // The map is full. This request must sweep expired entries synchronously, without
        // waiting for the scheduled cleanup, then use the freed slot.
        assertThat(service.isBlocked("new@example.com")).isFalse();

        // The sweep must not reclaim active lockouts or exceed the configured capacity.
        assertThat(service.isBlocked("active-blocked@example.com")).isTrue();
        assertThat(service.isBlocked("another-new@example.com")).isTrue();
    }

    @Test
    void concurrentRequestsDuringCapacityCleanupUseFreshTimeAndAdmitOnlyOneEmail() throws Exception {
        MutableClock clock = new MutableClock();
        CountDownLatch cleanupStarted = new CountDownLatch(1);
        CountDownLatch allowCleanup = new CountDownLatch(1);
        AtomicBoolean firstTickerCall = new AtomicBoolean(true);
        AtomicLong monotonicNanos = new AtomicLong();
        LoginAttemptService service = new LoginAttemptService(clock, () -> {
            if (firstTickerCall.compareAndSet(true, false)) {
                cleanupStarted.countDown();
                awaitLatch(allowCleanup);
            }
            return monotonicNanos.get();
        });

        service.loginFailed("expires-during-cleanup@example.com");
        clock.advance(Duration.ofMinutes(14).plusSeconds(59));
        for (int attempt = 0; attempt < 5; attempt++) {
            service.loginFailed("active-blocked@example.com");
        }
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 2; index++) {
            service.isBlocked("active-" + index + "@example.com");
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        CountDownLatch secondRequestStarted = new CountDownLatch(1);
        try {
            Future<Boolean> firstRequest = executor.submit(
                    () -> service.isBlocked("concurrent-one@example.com"));
            assertThat(cleanupStarted.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Boolean> secondRequest = executor.submit(() -> {
                waitingThread.set(Thread.currentThread());
                secondRequestStarted.countDown();
                return service.isBlocked("concurrent-two@example.com");
            });
            assertThat(secondRequestStarted.await(5, TimeUnit.SECONDS)).isTrue();
            awaitThreadState(waitingThread.get(), Thread.State.BLOCKED);

            // The first request captured its Instant before waiting in the cleanup monitor.
            // The candidate expires while cleanup is paused; the sweep must use a fresh Instant.
            clock.advance(Duration.ofSeconds(2));
            allowCleanup.countDown();

            int admitted = (firstRequest.get() ? 0 : 1) + (secondRequest.get() ? 0 : 1);
            assertThat(admitted).isEqualTo(1);
            assertThat(service.isBlocked("active-blocked@example.com")).isTrue();
            assertThat(service.isBlocked("third-concurrent@example.com")).isTrue();
        } finally {
            allowCleanup.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void anEntryExpiringInsideCapacityCleanupIntervalWaitsUntilTheIntervalPasses() {
        MutableClock clock = new MutableClock();
        AtomicLong monotonicNanos = new AtomicLong();
        LoginAttemptService service = new LoginAttemptService(clock, monotonicNanos::get);

        service.loginFailed("soon-expired@example.com");
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 1; index++) {
            service.isBlocked("active-" + index + "@example.com");
        }
        assertThat(service.isBlocked("overflow@example.com")).isTrue(); // Starts the 60-second throttle.

        clock.advance(Duration.ofMinutes(14));
        service.loginSucceeded("active-0@example.com");
        for (int attempt = 0; attempt < 5; attempt++) {
            service.loginFailed("active-blocked@example.com");
        }
        for (int index = 1; index < LoginAttemptService.MAX_TRACKED_EMAILS - 1; index++) {
            service.loginFailed("active-" + index + "@example.com");
        }

        clock.advance(Duration.ofMinutes(1).plusNanos(1));
        assertThat(service.isBlocked("overflow@example.com")).isTrue();

        monotonicNanos.addAndGet(TimeUnit.SECONDS.toNanos(60));
        assertThat(service.isBlocked("overflow@example.com")).isFalse();
        assertThat(service.isBlocked("active-blocked@example.com")).isTrue();
    }

    @Test
    void keepsAttemptCountersIsolatedAcrossEmails() {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());
        for (int attempt = 0; attempt < 5; attempt++) {
            service.loginFailed("blocked@example.com");
        }

        assertThat(service.isBlocked("blocked@example.com")).isTrue();
        assertThat(service.isBlocked("unblocked@example.com")).isFalse();
    }

    @Test
    void abortReleasesAnEmptyReservationButPreservesRecordedFailures() {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 1; index++) {
            service.isBlocked("filled-" + index + "@example.com");
        }
        assertThat(service.isBlocked("reserved@example.com")).isFalse();
        service.loginAborted("reserved@example.com");
        assertThat(service.isBlocked("another@example.com")).isFalse();

        LoginAttemptService withFailures = new LoginAttemptService(new MutableClock());
        withFailures.loginFailed("with-failures@example.com");
        withFailures.loginAborted("with-failures@example.com");
        for (int attempt = 1; attempt < 5; attempt++) {
            withFailures.loginFailed("with-failures@example.com");
        }
        assertThat(withFailures.isBlocked("with-failures@example.com")).isTrue();
    }

    @Test
    void concurrentFailuresCannotLoseUpdatesOrExceedTheAttemptLimit() throws Exception {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());
        service.isBlocked("parallel@example.com");

        int workers = 24;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();
        try {
            for (int worker = 0; worker < workers; worker++) {
                tasks.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    for (int attempt = 0; attempt < 100; attempt++) {
                        service.loginFailed("parallel@example.com");
                    }
                    return null;
                }));
            }

            ready.await();
            start.countDown();
            for (Future<?> task : tasks) {
                task.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(service.isBlocked("parallel@example.com")).isTrue();
    }

    @Test
    void concurrentNewEmailsCannotExceedTheConfiguredCapacity() throws Exception {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 1; index++) {
            service.isBlocked("reserved-" + index + "@example.com");
        }

        int workers = 32;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> tasks = new ArrayList<>();
        try {
            for (int worker = 0; worker < workers; worker++) {
                int emailIndex = worker;
                tasks.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return !service.isBlocked("racing-" + emailIndex + "@example.com");
                }));
            }

            ready.await();
            start.countDown();
            int admitted = 0;
            for (Future<Boolean> task : tasks) {
                if (task.get()) {
                    admitted++;
                }
            }
            assertThat(admitted).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for test coordination latch");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for test coordination latch", exception);
        }
    }

    private static void awaitThreadState(Thread thread, Thread.State expectedState) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline && thread.getState() != expectedState) {
            Thread.sleep(1);
        }
        assertThat(thread.getState()).isEqualTo(expectedState);
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));

        void advance(Duration duration) {
            instant.updateAndGet(current -> current.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
