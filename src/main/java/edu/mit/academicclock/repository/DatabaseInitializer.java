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
                    semester INT DEFAULT 3,
                    gender VARCHAR(32) DEFAULT 'Female',
                    password VARCHAR(128) DEFAULT 'clockit2026',
                    avatar_url CLOB,
                    is_doodle BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Migration safeguards for existing DB
            stmt.execute("ALTER TABLE students ADD COLUMN IF NOT EXISTS gender VARCHAR(32) DEFAULT 'Female';");
            stmt.execute("ALTER TABLE students ADD COLUMN IF NOT EXISTS password VARCHAR(128) DEFAULT 'clockit2026';");

            // 2. Subjects Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS subjects (
                    id VARCHAR(64) PRIMARY KEY,
                    code VARCHAR(32) UNIQUE NOT NULL,
                    name VARCHAR(128) NOT NULL,
                    units INT DEFAULT 5,
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

            System.out.println("✅ ClockIt database schema initialized successfully.");
            seedOrUpgrade(conn);

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to initialize database tables: " + e.getMessage(), e);
        }
    }

    private static void seedOrUpgrade(Connection conn) throws SQLException {
        // 1. Seed or update primary student profile
        upsertStudent(conn, "std-priyanthi-01", "2023506001", "Priyanthi A", "Computer Technology", "B.E. Computer Science and Engineering", 3, "Female", "clockit2026");
        upsertStudent(conn, "std-demo-01", "DEMO-ROLL-001", "Student", "Computer Technology", "B.E. Computer Science and Engineering", 3, "Female", "clockit2026");

        // 2. Seed official Anna University / MIT Campus Semester 3 CSE/CT Curriculum Subjects
        upsertSubject(conn, "MA23C05", "Probability and Statistics", 5, 65, 18, 20);
        upsertSubject(conn, "CS23301", "Software Engineering", 5, 50, 19, 20);
        upsertSubject(conn, "CS23302", "Data Structures", 5, 70, 17, 20);
        upsertSubject(conn, "CS23303", "Digital System Design", 5, 55, 16, 20);
        upsertSubject(conn, "CS23304", "Java Programming", 5, 80, 19, 20);
        upsertSubject(conn, "CS23U01", "Standards - Computer Science & Engg.", 2, 85, 9, 10);
        upsertSubject(conn, "UC23U01", "Universal Human Values", 5, 90, 10, 10);

        // 3. Seed official Anna University MIT Campus End Semester Examination Timetable (Nov/Dec 2026)
        upsertCalendar(conn, "cal-01", "Examination Timetable Published", "2026-10-22", "event", "Anna University Office of Additional Controller of Examinations");
        upsertCalendar(conn, "cal-02", "Internal Assessment 2 & Attendance Freeze", "2026-10-28", "deadline", "Final eligibility list publication · min 75% attendance");
        upsertCalendar(conn, "cal-03", "Hall Ticket Distribution & Model Lab Reviews", "2026-11-05", "deadline", "Collect signed hall tickets from Department Advisor");

        // Official End Semester Exam Timetable:
        upsertCalendar(conn, "cal-exam-01", "End Sem: MA23C05 Probability & Statistics", "2026-11-11", "exam", "FN (10:00 AM – 01:00 PM) · MIT Chromepet Campus · Reg 2023");
        upsertCalendar(conn, "cal-exam-02", "End Sem: CS23304 Java Programming", "2026-11-14", "exam", "FN (10:00 AM – 01:00 PM) · MIT Chromepet Campus · Reg 2023");
        upsertCalendar(conn, "cal-exam-03", "End Sem: CS23303 Digital System Design", "2026-11-18", "exam", "AN (02:00 PM – 05:00 PM) · MIT Chromepet Campus · Reg 2023");
        upsertCalendar(conn, "cal-exam-04", "End Sem: CS23302 Data Structures", "2026-11-25", "exam", "AN (02:00 PM – 05:00 PM) · MIT Chromepet Campus · Reg 2023");
        upsertCalendar(conn, "cal-exam-05", "End Sem: CS23301 Software Engineering", "2026-11-30", "exam", "FN (10:00 AM – 01:00 PM) · MIT Chromepet Campus · Reg 2023");

        upsertCalendar(conn, "cal-04", "End Sem Practical Lab Examinations", "2026-12-04", "exam", "CS23304 Java Programming & CS23302 Data Structures Lab");
        upsertCalendar(conn, "cal-05", "Winter Break & Semester 4 Reopening Prep", "2026-12-15", "event", "Campus reopening notice for academic year 2026–2027");

        // 4. Seed Campus Events
        upsertEvent(conn, "evt-01", "Student Tech Meetup", "TECH TALK", "2026-10-25", "4:00 PM", "Rajam Hall, MIT Campus", "Talks, coding demos and peer tech discussions on full-stack Java development.", false);
        upsertEvent(conn, "evt-02", "End-Sem Java & DSA Peer Study Sprint", "WORKSHOP", "2026-11-06", "10:00 AM", "Computer Tech Lab 2, MIT", "Focused peer revision on Unit I–V concepts, multithreading, and trees.", true);
        upsertEvent(conn, "evt-03", "Creative Club Afternoon", "CAMPUS LIFE", "2026-11-12", "3:00 PM", "Hostel Quadrangle / Open Air", "A calm study-break corner with music, sketching, and chai before exams.", false);

        // 5. Seed Marks
        upsertMark(conn, "mrk-01", "Probability and Statistics", "Assessment 1 (Written)", 18.5, 20);
        upsertMark(conn, "mrk-02", "Java Programming", "Lab Assessment (JDBC & Concurrency)", 19.0, 20);
        upsertMark(conn, "mrk-03", "Data Structures", "Quiz 1 (Binary Trees)", 17.5, 20);
        upsertMark(conn, "mrk-04", "Digital System Design", "Class Test (Sequential Logic)", 16.0, 20);
        upsertMark(conn, "mrk-05", "Software Engineering", "SRS & Agile Sprint Review", 18.0, 20);

        // 6. Seed Journal Entry
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM journal_entries");
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO journal_entries (id, title, content, mood, entry_date) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, "jrn-01");
                    ps.setString(2, "End-Sem Preparation & Small Steps");
                    ps.setString(3, "The exam schedule is out for November 2026. Starting revision with Probability & Statistics and Java Programming. One day at a time. ✿");
                    ps.setString(4, "🌱");
                    ps.setDate(5, java.sql.Date.valueOf("2026-10-22"));
                    ps.executeUpdate();
                }
            }
        }

        System.out.println("✨ ClockIt database seeded with official Semester 3 syllabus & exam schedule.");
    }

    private static void upsertStudent(Connection conn, String id, String roll, String name, String dept, String prog, int sem, String gender, String pass) throws SQLException {
        String check = "SELECT id FROM students WHERE roll_number = ? OR id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, roll);
            ps.setString(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existingId = rs.getString("id");
                    String update = "UPDATE students SET roll_number = ?, name = ?, department = ?, programme = ?, semester = ?, gender = ?, password = ? WHERE id = ?;";
                    try (PreparedStatement ups = conn.prepareStatement(update)) {
                        ups.setString(1, roll);
                        ups.setString(2, name);
                        ups.setString(3, dept);
                        ups.setString(4, prog);
                        ups.setInt(5, sem);
                        ups.setString(6, gender);
                        ups.setString(7, pass);
                        ups.setString(8, existingId);
                        ups.executeUpdate();
                    }
                    return;
                }
            }
        }
        String insert = "INSERT INTO students (id, roll_number, name, department, programme, semester, gender, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ips = conn.prepareStatement(insert)) {
            ips.setString(1, id);
            ips.setString(2, roll);
            ips.setString(3, name);
            ips.setString(4, dept);
            ips.setString(5, prog);
            ips.setInt(6, sem);
            ips.setString(7, gender);
            ips.setString(8, pass);
            ips.executeUpdate();
        }
    }

    private static void upsertSubject(Connection conn, String code, String name, int units, int progress, int attended, int total) throws SQLException {
        String check = "SELECT id FROM subjects WHERE code = ?;";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existingId = rs.getString("id");
                    String update = "UPDATE subjects SET name = ?, units = ?, progress = ? WHERE id = ?;";
                    try (PreparedStatement ups = conn.prepareStatement(update)) {
                        ups.setString(1, name);
                        ups.setInt(2, units);
                        ups.setInt(3, progress);
                        ups.setString(4, existingId);
                        ups.executeUpdate();
                    }
                    return;
                }
            }
        }
        String insert = "INSERT INTO subjects (id, code, name, units, progress, attended, total) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ips = conn.prepareStatement(insert)) {
            ips.setString(1, "sub-" + code.toLowerCase());
            ips.setString(2, code);
            ips.setString(3, name);
            ips.setInt(4, units);
            ips.setInt(5, progress);
            ips.setInt(6, attended);
            ips.setInt(7, total);
            ips.executeUpdate();
        }
    }

    private static void upsertCalendar(Connection conn, String id, String title, String date, String type, String detail) throws SQLException {
        String check = "SELECT id FROM calendar_items WHERE id = ? OR (item_date = ? AND title = ?);";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, id);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setString(3, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existingId = rs.getString("id");
                    String update = "UPDATE calendar_items SET title = ?, item_date = ?, item_type = ?, detail = ? WHERE id = ?;";
                    try (PreparedStatement ups = conn.prepareStatement(update)) {
                        ups.setString(1, title);
                        ups.setDate(2, java.sql.Date.valueOf(date));
                        ups.setString(3, type);
                        ups.setString(4, detail);
                        ups.setString(5, existingId);
                        ups.executeUpdate();
                    }
                    return;
                }
            }
        }
        String insert = "INSERT INTO calendar_items (id, title, item_date, item_type, detail) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement ips = conn.prepareStatement(insert)) {
            ips.setString(1, id);
            ips.setString(2, title);
            ips.setDate(3, java.sql.Date.valueOf(date));
            ips.setString(4, type);
            ips.setString(5, detail);
            ips.executeUpdate();
        }
    }

    private static void upsertEvent(Connection conn, String id, String title, String category, String date, String time, String venue, String desc, boolean registered) throws SQLException {
        String check = "SELECT id FROM campus_events WHERE id = ? OR title = ?;";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, id);
            ps.setString(2, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String insert = "INSERT INTO campus_events (id, title, category, event_date, event_time, venue, description, is_registered) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ips = conn.prepareStatement(insert)) {
            ips.setString(1, id);
            ips.setString(2, title);
            ips.setString(3, category);
            ips.setDate(4, java.sql.Date.valueOf(date));
            ips.setString(5, time);
            ips.setString(6, venue);
            ips.setString(7, desc);
            ips.setBoolean(8, registered);
            ips.executeUpdate();
        }
    }

    private static void upsertMark(Connection conn, String id, String subject, String assessment, double score, double outOf) throws SQLException {
        String check = "SELECT id FROM assessment_marks WHERE id = ? OR (subject = ? AND assessment = ?);";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, id);
            ps.setString(2, subject);
            ps.setString(3, assessment);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String insert = "INSERT INTO assessment_marks (id, subject, assessment, score, out_of) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement ips = conn.prepareStatement(insert)) {
            ips.setString(1, id);
            ips.setString(2, subject);
            ips.setString(3, assessment);
            ips.setDouble(4, score);
            ips.setDouble(5, outOf);
            ips.executeUpdate();
        }
    }
}
