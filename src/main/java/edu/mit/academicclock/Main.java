package edu.mit.academicclock;

import edu.mit.academicclock.repository.AttendanceRepository;
import edu.mit.academicclock.repository.CalendarRepository;
import edu.mit.academicclock.repository.DatabaseInitializer;
import edu.mit.academicclock.repository.EventRepository;
import edu.mit.academicclock.repository.JournalRepository;
import edu.mit.academicclock.repository.MarksRepository;
import edu.mit.academicclock.repository.StudentRepository;
import edu.mit.academicclock.repository.SubjectRepository;
import edu.mit.academicclock.service.AcademicService;
import edu.mit.academicclock.service.BackgroundScheduler;
import edu.mit.academicclock.web.ApiHandlers;
import edu.mit.academicclock.web.HttpRouter;

import java.io.File;

/**
 * CS23304 JAVA PROGRAMMING - Capstone Application
 * Main Application Bootstrapper
 * 
 * Maps directly to CS23304 Units:
 * - Unit I: OOP, Classes, Constructors, Date/Time API
 * - Unit II: Polymorphism, Inheritance, Abstract Classes & Interfaces
 * - Unit III: Exception Handling, Multithreading & Synchronization
 * - Unit IV: Collections, Streams API, Lambda Expressions & JDBC Database
 * - Unit V: Web Framework, HTTP Server & REST Architecture
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("⏰ CLOCKIT — ACADEMIC CALENDAR & STUDY SPACE");
        System.out.println("   Powered by Java, Embedded H2 SQL & Anna University Curriculum");
        System.out.println("==========================================================");

        try {
            // 1. Initialize Database Schema & Seed Data (Unit IV JDBC)
            DatabaseInitializer.initialize();

            // 2. Initialize Repositories (Unit IV Generics & Collections)
            SubjectRepository subjectRepo = new SubjectRepository();
            CalendarRepository calendarRepo = new CalendarRepository();
            EventRepository eventRepo = new EventRepository();
            MarksRepository marksRepo = new MarksRepository();
            JournalRepository journalRepo = new JournalRepository();
            StudentRepository studentRepo = new StudentRepository();
            AttendanceRepository attendanceRepo = new AttendanceRepository();

            // 3. Initialize Service Layer (Unit IV Streams & Lambdas)
            AcademicService service = new AcademicService(
                    subjectRepo,
                    calendarRepo,
                    eventRepo,
                    marksRepo,
                    journalRepo,
                    studentRepo,
                    attendanceRepo
            );

            // 4. Start Background Scheduler Daemon (Unit III Multithreading)
            BackgroundScheduler scheduler = new BackgroundScheduler(calendarRepo);
            scheduler.start();

            // 5. Start Web Server & REST API (Unit V Web Framework)
            int port = 8080;
            String portEnv = System.getenv("PORT");
            if (portEnv != null && !portEnv.isBlank()) {
                try {
                    port = Integer.parseInt(portEnv.trim());
                } catch (NumberFormatException ignored) {}
            }

            File publicDir = new File("public");
            ApiHandlers apiHandlers = new ApiHandlers(service);
            HttpRouter router = new HttpRouter(port, apiHandlers, publicDir);
            router.start();

            // 6. Graceful JVM Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nStopping ClockIt services...");
                scheduler.stop();
                router.stop();
                System.out.println("Goodbye from ClockIt! ✿");
            }));

        } catch (Exception e) {
            System.err.println("❌ Fatal startup error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
