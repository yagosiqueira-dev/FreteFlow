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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    @Test
    void tracksAtMostFiveFailuresAndSuccessClearsTheHistory() {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());

        for (int attempt = 0; attempt < 8; attempt++) {
            service.loginFailed("person@example.com");
        }

        assertThat(service.trackedFailureCount("person@example.com")).isEqualTo(5);
        service.loginSucceeded("person@example.com");
        assertThat(service.trackedFailureCount("person@example.com")).isZero();
    }

    @Test
    void expiresFailureHistoryAfterFifteenMinutes() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);
        service.loginFailed("expired@example.com");

        clock.advance(Duration.ofMinutes(15));
        assertThat(service.removeExpiredAttempts(clock.instant())).isZero();

        clock.advance(Duration.ofNanos(1));
        assertThat(service.removeExpiredAttempts(clock.instant())).isEqualTo(1);
        assertThat(service.trackedFailureCount("expired@example.com")).isZero();
    }

    @Test
    void arbitraryEmailsCannotGrowTrackingBeyondTenThousandEntries() {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());

        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS; index++) {
            service.loginFailed("user-" + index + "@example.com");
        }
        service.loginFailed("overflow@example.com");
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS; index++) {
            service.loginFailed("unknown-" + index + "@example.com");
        }

        assertThat(service.trackedEmailCount()).isEqualTo(LoginAttemptService.MAX_TRACKED_EMAILS);
        assertThat(service.trackedFailureCount("overflow@example.com")).isZero();

        service.loginSucceeded("user-0@example.com");
        service.loginFailed("overflow@example.com");
        assertThat(service.trackedEmailCount()).isEqualTo(LoginAttemptService.MAX_TRACKED_EMAILS);
        assertThat(service.trackedFailureCount("overflow@example.com")).isEqualTo(1);
    }

    @Test
    void arbitraryEmailsAreTrackedOnlyUpToTheCapacityLimit() {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());

        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS * 2; index++) {
            service.loginFailed("unknown-" + index + "@example.com");
        }

        assertThat(service.trackedEmailCount()).isEqualTo(LoginAttemptService.MAX_TRACKED_EMAILS);
        assertThat(service.trackedFailureCount("unknown-0@example.com")).isEqualTo(1);
        assertThat(service.trackedFailureCount("unknown-" + LoginAttemptService.MAX_TRACKED_EMAILS
                + "@example.com")).isZero();
    }

    @Test
    void concurrentFailuresDoNotCorruptCounters() throws Exception {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());
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

        assertThat(service.trackedEmailCount()).isEqualTo(1);
        assertThat(service.trackedFailureCount("parallel@example.com")).isEqualTo(5);
    }

    @Test
    void concurrentDistinctEmailsRespectTheHardCapacity() throws Exception {
        LoginAttemptService service = new LoginAttemptService(new MutableClock());
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 1; index++) {
            service.loginFailed("reserved-" + index + "@example.com");
        }

        int workers = 32;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();
        try {
            for (int worker = 0; worker < workers; worker++) {
                int emailIndex = worker;
                tasks.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    service.loginFailed("parallel-" + emailIndex + "@example.com");
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

        assertThat(service.trackedEmailCount()).isEqualTo(LoginAttemptService.MAX_TRACKED_EMAILS);
    }

    @Test
    void expiredEntriesAreRemovedUnderCapacityPressureWithoutGrowingTheMap() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(clock);
        service.loginFailed("expired@example.com");
        for (int index = 0; index < LoginAttemptService.MAX_TRACKED_EMAILS - 1; index++) {
            service.loginFailed("active-" + index + "@example.com");
        }

        clock.advance(Duration.ofMinutes(15).plusNanos(1));
        service.loginFailed("new@example.com");

        assertThat(service.trackedEmailCount()).isEqualTo(1);
        assertThat(service.trackedFailureCount("expired@example.com")).isZero();
        assertThat(service.trackedFailureCount("new@example.com")).isEqualTo(1);
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
