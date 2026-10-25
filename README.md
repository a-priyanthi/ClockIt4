# 🎓 MIT Academic Clock — Fullstack Java Platform

> **Interactive Academic Year Planner & Student Workspace**  
> Built for Course **CS23304: Java Programming** with an embedded SQL Database, JDBC connectivity, Multithreading, and REST Web Architecture.

---

## 🌟 Overview

**MIT Academic Clock** is an interactive, calming academic companion designed for students at MIT Campus, Anna University. It brings together academic deadlines, course curriculum progress, attendance tracking, campus events, assessment grades, and personal reflection into one cohesive workspace.

At the heart of the system is the **Annual Academic Clock**: a circular planner mapping the 12 months, daily graduations, and weekly divisions with dynamic activity pins that synchronize live with an embedded SQL database.

---

## 🏛️ CS23304 Syllabus Topics Architecture Mapping

This project was built from scratch to embody the core principles and practical requirements of the **CS23304 Java Programming** syllabus:

### 🔹 UNIT I — Introduction to Java & Date/Time API
- **Classes, Objects & Constructors**:
  - Encapsulated entity models: [`Student`](file:///src/main/java/edu/mit/academicclock/model/Student.java), [`Subject`](file:///src/main/java/edu/mit/academicclock/model/Subject.java), [`AttendanceRecord`](file:///src/main/java/edu/mit/academicclock/model/AttendanceRecord.java).
- **Static Members & Constants**:
  - Connection parameters and configuration in [`DatabaseConfig`](file:///src/main/java/edu/mit/academicclock/config/DatabaseConfig.java).
- **String Class & Processing**:
  - Dynamic parsing and unescaping in [`SimpleJson`](file:///src/main/java/edu/mit/academicclock/web/SimpleJson.java).
- **Modern Date & Time API (`java.time`)**:
  - Extensively uses `LocalDate`, `LocalDateTime`, `Timestamp`, and `DateTimeFormatter` for scheduling throughout [`CalendarItem`](file:///src/main/java/edu/mit/academicclock/model/CalendarItem.java) and [`CampusEvent`](file:///src/main/java/edu/mit/academicclock/model/CampusEvent.java).

### 🔹 UNIT II — Polymorphism & Inheritance
- **Interfaces**:
  - Contract abstractions: [`Identifiable`](file:///src/main/java/edu/mit/academicclock/model/Identifiable.java), [`Schedulable`](file:///src/main/java/edu/mit/academicclock/model/Schedulable.java), and generic [`CrudRepository<T, ID>`](file:///src/main/java/edu/mit/academicclock/repository/CrudRepository.java).
- **Abstract Classes & Superclasses**:
  - [`BaseEntity`](file:///src/main/java/edu/mit/academicclock/model/BaseEntity.java) provides baseline ID and audit timestamps (`createdAt`, `updatedAt`).
  - [`AcademicItem`](file:///src/main/java/edu/mit/academicclock/model/AcademicItem.java) acts as the polymorphic base for calendar deadlines and campus events.
- **Method Overloading & Overriding**:
  - Overloaded query methods in [`CalendarRepository`](file:///src/main/java/edu/mit/academicclock/repository/CalendarRepository.java) (`findByDate(LocalDate)` vs `findByDateRange(LocalDate, LocalDate)`).
  - Overridden `toString()`, `getId()`, `getSummary()` across entity subclasses.

### 🔹 UNIT III — Exception Handling & Multithreading
- **Custom User-Defined Exception Hierarchy**:
  - [`AcademicClockException`](file:///src/main/java/edu/mit/academicclock/exception/AcademicClockException.java) (Base domain exception)
  - [`ResourceNotFoundException`](file:///src/main/java/edu/mit/academicclock/exception/ResourceNotFoundException.java) (404 status mapping)
  - [`ValidationException`](file:///src/main/java/edu/mit/academicclock/exception/ValidationException.java) (400 validation failures)
  - [`DatabaseOperationException`](file:///src/main/java/edu/mit/academicclock/exception/DatabaseOperationException.java) (JDBC SQL exception wrapper)
- **Multithreading & Synchronization**:
  - [`BackgroundScheduler`](file:///src/main/java/edu/mit/academicclock/service/BackgroundScheduler.java): A daemon background thread worker with custom `ThreadFactory` and priorities that periodically checks approaching deadlines and performs audit tasks.
  - Utilizes `ReentrantLock` and `synchronized` blocks to guarantee thread safety and prevent deadlocks.
  - Multi-threaded worker pool (`Executors.newFixedThreadPool(16)`) serving HTTP web client requests concurrently.

### 🔹 UNIT IV — File Streams, Collections & Database (JDBC)
- **Generics & Generic Collections**:
  - Generic interface [`CrudRepository<T, ID>`](file:///src/main/java/edu/mit/academicclock/repository/CrudRepository.java) providing type-safe persistence.
  - Uses `List<T>`, `Map<K, V>`, and `Set<T>` across all services.
- **Java Streams API & Lambda Expressions**:
  - Implemented throughout [`AcademicService`](file:///src/main/java/edu/mit/academicclock/service/AcademicService.java):
    - `.stream().filter(...).sorted(...).limit(4).collect(...)` for sorting upcoming deadlines.
    - `.stream().mapToDouble(...).average()` for calculating aggregate attendance percentages across subjects.
    - Global multi-entity search filtering using Lambda predicates.
- **Database Connectivity with JDBC**:
  - Zero-configuration embedded **H2 SQL Database Engine** via standard `java.sql.Connection`, `PreparedStatement`, `Statement`, and `ResultSet`.
  - Self-healing schema initialization and seeding in [`DatabaseInitializer`](file:///src/main/java/edu/mit/academicclock/repository/DatabaseInitializer.java).
  - File-based persistence saved to `./data/academic_clock.mv.db`.

### 🔹 UNIT V — Web Development & Frameworks
- **Java HTTP Server & REST Architecture**:
  - Built-in multi-threaded HTTP server [`HttpRouter`](file:///src/main/java/edu/mit/academicclock/web/HttpRouter.java) mounted with comprehensive REST API handlers in [`ApiHandlers`](file:///src/main/java/edu/mit/academicclock/web/ApiHandlers.java).
  - Serves dynamic REST endpoints on `/api/*` and delivers static assets (`index.html`, `style.css`, `app.js`) with MIME type detection.
- **Spring Boot & Maven Compatibility**:
  - Includes a standard [`pom.xml`](file:///pom.xml) enabling 1-click import into NetBeans, Eclipse, IntelliJ, or Spring Boot.

---

## 🗄️ Database Tables (H2 SQL)

| Table | Primary Key | Description |
|---|---|---|
| `students` | `id` | Student profiles (Roll number, Name, Department, Degree, Semester, Avatar) |
| `subjects` | `id` | Academic subjects (Code, Name, Units, Progress %, Attended, Total) |
| `calendar_items` | `id` | Scheduled milestones (Deadlines, Exams, Events, Completed tasks) |
| `campus_events` | `id` | University events (Title, Category, Date, Time, Venue, Registration state) |
| `attendance_records` | `id` | Daily attendance audit log (Subject code, Date, Status: Present/Absent) |
| `assessment_marks` | `id` | Academic grades (Subject, Assessment title, Score, Out of) |
| `journal_entries` | `id` | Scrapbook journal pages (Title, Reflection content, Mood, Date) |

---

## 🚀 How to Run

### Option 1: 1-Click Launch (Windows)
Double-click [`run.bat`](file:///run.bat) or run from PowerShell:
```powershell
cd "C:\Users\PRIYANTHI A\.gemini\antigravity-ide\scratch\academic-clock"
.\run.bat
```
*Compiles all Java sources, boots the server, and automatically opens `http://localhost:8080` in your browser!*

### Option 2: Build & Run with Standard Java CLI
```bash
# 1. Compile
build.bat

# 2. Run
java -cp "bin;lib/h2.jar" edu.mit.academicclock.Main
```

### Option 3: Import into NetBeans / Eclipse / IntelliJ
1. Open your IDE.
2. Click **File** → **Open Project** (or **Import Maven Project**).
3. Select this folder: `C:\Users\PRIYANTHI A\.gemini\antigravity-ide\scratch\academic-clock`.
4. Run `edu.mit.academicclock.Main`.

---

## 🧪 Automated Integration Tests

Run the built-in test suite to verify all database and API operations:
```bash
node test-all.js
```

---

## 📄 License
Created for academic demonstration and learning under the MIT License.
