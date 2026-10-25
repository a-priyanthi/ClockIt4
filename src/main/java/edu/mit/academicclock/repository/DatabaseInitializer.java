package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Unit IV: Databases with JDBC
 * Creates tables and seeds initial academic schedule.
 */
public class DatabaseInitializer {

    public static synchronized void initialize() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Students Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id VARCHAR(64) PRIMARY KEY,
                    roll_number VARCHAR(64) UNIQUE NOT NULL,
                    name VARCHAR(128) NOT NULL,
                    department VARCHAR(128),
                    programme VARCHAR(128),
                    semester INT DEFAULT 1,
                    avatar_url CLOB,
                    is_doodle BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 2. Subjects Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS subjects (
                    id VARCHAR(64) PRIMARY KEY,
                    code VARCHAR(32) UNIQUE NOT NULL,
                    name VARCHAR(128) NOT NULL,
                    units INT DEFAULT 4,
                    progress INT DEFAULT 0,
                    attended INT DEFAULT 0,
                    total INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 3. Calendar Items Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS calendar_items (
                    id VARCHAR(64) PRIMARY KEY,
                    title VARCHAR(256) NOT NULL,
                    item_date DATE NOT NULL,
                    item_type VARCHAR(64) NOT NULL,
                    detail VARCHAR(512),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 4. Campus Events Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS campus_events (
                    id VARCHAR(64) PRIMARY KEY,
                    title VARCHAR(256) NOT NULL,
                    category VARCHAR(64) NOT NULL,
                    event_date DATE NOT NULL,
                    event_time VARCHAR(64),
                    venue VARCHAR(256),
                    description CLOB,
                    is_registered BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 5. Attendance Records Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance_records (
                    id VARCHAR(64) PRIMARY KEY,
                    subject_code VARCHAR(32) NOT NULL,
                    record_date DATE NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 6. Assessment Marks Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS assessment_marks (
                    id VARCHAR(64) PRIMARY KEY,
                    subject VARCHAR(128) NOT NULL,
                    assessment VARCHAR(128) NOT NULL,
                    score DOUBLE NOT NULL,
                    out_of DOUBLE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 7. Journal Entries Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS journal_entries (
                    id VARCHAR(64) PRIMARY KEY,
                    title VARCHAR(256) NOT NULL,
                    content CLOB,
                    mood VARCHAR(32) DEFAULT '🙂',
                    entry_date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            System.out.println("✅ Database schema initialized successfully.");
            seedIfEmpty(conn);

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to initialize database tables: " + e.getMessage(), e);
        }
    }

    private static void seedIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM students");
            if (rs.next() && rs.getInt("cnt") == 0) {
                System.out.println("🌱 Seeding database with MIT Chennai academic prototype data...");

                // 1. Seed Student
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO students (id, roll_number, name, department, programme, semester) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, "std-demo-01");
                    ps.setString(2, "DEMO-ROLL-001");
                    ps.setString(3, "Student");
                    ps.setString(4, "Computer Technology");
                    ps.setString(5, "B.Tech Computer Technology");
                    ps.setInt(6, 4);
                    ps.executeUpdate();
                }

                // 2. Seed Subjects
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO subjects (id, code, name, units, progress, attended, total) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    insertSubject(ps, "sub-01", "SUB-01", "Data Structures", 5, 60, 18, 20);
                    insertSubject(ps, "sub-02", "SUB-02", "Programming", 5, 45, 16, 20);
                    insertSubject(ps, "sub-03", "SUB-03", "Mathematics", 4, 75, 17, 20);
                    insertSubject(ps, "sub-04", "SUB-04", "Digital Systems", 5, 35, 15, 20);
                    insertSubject(ps, "sub-05", "SUB-05", "Communication Skills", 3, 80, 18, 20);
                }

                // 3. Seed Calendar Items
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO calendar_items (id, title, item_date, item_type, detail) VALUES (?, ?, ?, ?, ?)")) {
                    insertCalendar(ps, "cal-01", "Data Structures assignment", "2026-10-22", "deadline", "Demo due date · 11:59 PM");
                    insertCalendar(ps, "cal-02", "Student tech meetup", "2026-10-25", "event", "Demo event · MIT campus (unverified)");
                    insertCalendar(ps, "cal-03", "Lab revision reminder", "2026-10-28", "exam", "Personal planning reminder · not an official exam");
                    insertCalendar(ps, "cal-04", "Lab record check", "2026-11-03", "deadline", "Sample deadline · update from your faculty notice");
                    insertCalendar(ps, "cal-05", "Creative club afternoon", "2026-11-06", "event", "Demo event · location to be confirmed");
                }

                // 4. Seed Campus Events
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO campus_events (id, title, category, event_date, event_time, venue, description, is_registered) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    insertEvent(ps, "evt-01", "Student Tech Meetup", "TECH TALK", "2026-10-25", "4:00 PM", "Location to be confirmed", "A sample event card for talks, demos and student conversations.", false);
                    insertEvent(ps, "evt-02", "Web Dev Workshop", "WORKSHOP", "2026-11-05", "10:00 AM", "Venue to be confirmed", "A demo workshop listing. Verify organiser details before using.", false);
                    insertEvent(ps, "evt-03", "Creative Club Afternoon", "CAMPUS LIFE", "2026-11-12", "3:00 PM", "Location to be confirmed", "A relaxed sample listing for creative activities and meeting people.", false);
                }

                // 5. Seed Marks
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO assessment_marks (id, subject, assessment, score, out_of) VALUES (?, ?, ?, ?, ?)")) {
                    insertMark(ps, "mrk-01", "Data Structures", "Assignment 1", 18, 20);
                    insertMark(ps, "mrk-02", "Programming", "Lab exercise", 16, 20);
                    insertMark(ps, "mrk-03", "Mathematics", "Quiz 1", 17, 20);
                    insertMark(ps, "mrk-04", "Digital Systems", "Class test", 15, 20);
                }

                // 6. Seed Journal Entry
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO journal_entries (id, title, content, mood, entry_date) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, "jrn-01");
                    ps.setString(2, "A good day, in little ways");
                    ps.setString(3, "Today I showed up for myself. I can take one step at a time, and that still counts. ✿");
                    ps.setString(4, "🙂");
                    ps.setDate(5, java.sql.Date.valueOf("2026-10-20"));
                    ps.executeUpdate();
                }

                System.out.println("✨ Academic Clock database seeded successfully.");
            }
        }
    }

    private static void insertSubject(PreparedStatement ps, String id, String code, String name, int units, int progress, int attended, int total) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, code);
        ps.setString(3, name);
        ps.setInt(4, units);
        ps.setInt(5, progress);
        ps.setInt(6, attended);
        ps.setInt(7, total);
        ps.executeUpdate();
    }

    private static void insertCalendar(PreparedStatement ps, String id, String title, String date, String type, String detail) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, title);
        ps.setDate(3, java.sql.Date.valueOf(date));
        ps.setString(4, type);
        ps.setString(5, detail);
        ps.executeUpdate();
    }

    private static void insertEvent(PreparedStatement ps, String id, String title, String category, String date, String time, String venue, String desc, boolean registered) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, title);
        ps.setString(3, category);
        ps.setDate(4, java.sql.Date.valueOf(date));
        ps.setString(5, time);
        ps.setString(6, venue);
        ps.setString(7, desc);
        ps.setBoolean(8, registered);
        ps.executeUpdate();
    }

    private static void insertMark(PreparedStatement ps, String id, String subject, String assessment, double score, double outOf) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, subject);
        ps.setString(3, assessment);
        ps.setDouble(4, score);
        ps.setDouble(5, outOf);
        ps.executeUpdate();
    }
}
