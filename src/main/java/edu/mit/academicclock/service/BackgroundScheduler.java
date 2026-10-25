package edu.mit.academicclock.service;

import edu.mit.academicclock.model.CalendarItem;
import edu.mit.academicclock.repository.CalendarRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unit III: Multithreading and Synchronization
 * Background scheduler daemon for automated deadline alerts,
 * demonstrating thread creation, thread priority, synchronization locks,
 * and deadlock avoidance patterns.
 */
public class BackgroundScheduler {
    private final CalendarRepository calendarRepo;
    private final ScheduledExecutorService scheduler;
    private final ReentrantLock alertLock = new ReentrantLock(); // Synchronization and Deadlock prevention
    private volatile boolean running = false;

    public BackgroundScheduler(CalendarRepository calendarRepo) {
        this.calendarRepo = calendarRepo;

        // Custom ThreadFactory demonstrating Thread Name and Priorities (Unit III)
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "AcademicClock-Scheduler-" + count.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1); // Background lower priority
                return t;
            }
        };

        this.scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    public synchronized void start() {
        if (running) return;
        running = true;

        // Periodic background task executing every 30 seconds
        scheduler.scheduleAtFixedRate(this::checkUpcomingDeadlines, 5, 30, TimeUnit.SECONDS);
        System.out.println("🧵 Background Scheduler thread started (Unit III Multithreading).");
    }

    /**
     * Thread-safe critical section with explicit lock acquisition
     * to avoid deadlocks (Unit III: Synchronization & Avoiding Deadlocks).
     */
    private void checkUpcomingDeadlines() {
        if (!alertLock.tryLock()) {
            // Avoid blocking indefinitely / prevent deadlocks
            return;
        }

        try {
            LocalDate today = LocalDate.of(2026, 10, 20); // demo anchor date
            LocalDate targetWindow = today.plusDays(7);

            List<CalendarItem> upcoming = calendarRepo.findByDateRange(today, targetWindow);
            if (!upcoming.isEmpty()) {
                System.out.println("[Scheduler-Worker] 🔔 Active Reminders Check: " + upcoming.size() + " items approaching in the next 7 days.");
            }
        } catch (Exception e) {
            System.err.println("[Scheduler-Worker] Error checking deadlines: " + e.getMessage());
        } finally {
            alertLock.unlock();
        }
    }

    public synchronized void stop() {
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("🧵 Background Scheduler thread stopped.");
    }
}
