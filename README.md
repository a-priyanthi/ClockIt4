# ⏰ ClockIt — Fullstack Java Academic Platform

> **Interactive Academic Calendar, End-Semester Timetable & Study Space**  
> Built for Course **CS23304: Java Programming** with an embedded SQL Database, JDBC connectivity, Multithreading, Gated Authentication, and REST Web Architecture.  
> Aligned with **Anna University (MIT Campus Chromepet) Regulation 2023 / Revised 2024 (Semester 3 CSE/CT)**.

---

## 🌟 Overview

**ClockIt** is an interactive, calming academic companion designed for engineering students at Anna University / MIT Campus Chromepet. It brings together official End-Semester exam timetables, course curriculum unit progress, attendance tracking with eligibility indicators, campus study sprints, assessment grades, and personal reflection into one cohesive workspace.

At the heart of the system is the **ClockIt Annual Wheel**: a circular planner mapping the 12 months, daily graduations, and weekly divisions with dynamic activity pins that synchronize live with an embedded H2 SQL database.

### 🔒 Gated Access Control
Access to the ClockIt platform is securely gated behind a student sign-in/registration screen. On registration or sign-in, students provide:
1. **Full Name**
2. **Department** (e.g. Computer Technology, Computer Science & Engineering, IT, ECE, AI&DS)
3. **Roll Number** (e.g. 2023506001)
4. **Semester** (e.g. Semester 3)
5. **Gender** (Female, Male, Non-binary, Prefer not to say)
6. **Password**

Once authenticated, the full workspace is unlocked with a personalized topbar badge, attendance status, and customized timetable. A prominent **Sign Out** button allows locking the space anytime.

---

## 📅 Official End-Semester Examination Timetable (Nov / Dec 2026)

ClockIt comes pre-configured with the official Phase-11 Examination Timetable published by the **Office of the Additional Controller of Examinations, Anna University (MIT Chromepet Campus, Regulation 2023, Semester 3 CSE/CT)**:

| Date | Day | Session | Time | Course Code | Course Title | Category |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **11-NOV-2026** | Wednesday | Forenoon (FN) | 10:00 AM – 01:00 PM | `MA23C05` | **Probability and Statistics** | HSMC (4 Credits) |
| **14-NOV-2026** | Saturday | Forenoon (FN) | 10:00 AM – 01:00 PM | `CS23304` | **Java Programming** | PCC (5 Credits) |
| **18-NOV-2026** | Wednesday | Afternoon (AN) | 02:00 PM – 05:00 PM | `CS23303` | **Digital System Design** | ESC (5 Credits) |
| **25-NOV-2026** | Wednesday | Afternoon (AN) | 02:00 PM – 05:00 PM | `CS23302` | **Data Structures** | PCC (5 Credits) |
| **30-NOV-2026** | Monday | Forenoon (FN) | 10:00 AM – 01:00 PM | `CS23301` | **Software Engineering** | PCC (3 Credits) |

Each examination is highlighted with distinctive violet pins on the ClockIt circular planner, marked in the interactive Calendar, and featured on the Dashboard countdown cards.

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

### 🔹 UNIT IV — Generic Collections, Streams API & JDBC Database
- **Generic Repository Architecture**:
  - [`CrudRepository<T, ID>`](file:///src/main/java/edu/mit/academicclock/repository/CrudRepository.java) providing generic type-safe CRUD operations.
- **Java Streams API & Lambda Expressions**:
  - Average attendance computation across all registered subjects (`allSubjects.stream().filter(...).mapToDouble(...).average()`).
  - Upcoming deadline sorting and filtering (`allCalendarItems.stream().filter(...).sorted(...).limit(4)`).
  - Multi-dimensional global search across subjects, events, and calendar dates using lambdas.
- **Database Connectivity via JDBC**:
  - Thread-safe connection pool pointing to embedded H2 SQL database [`data/academic_clock.mv.db`](file:///data/academic_clock.mv.db).
  - 7 relational tables: `students`, `subjects`, `calendar_items`, `campus_events`, `attendance_records`, `assessment_marks`, `journal_entries`.
  - Secure SQL execution with `PreparedStatement`, parameterized values, transactions, and idempotent `MERGE INTO` queries.

### 🔹 UNIT V — Web Development & Frameworks
- **Multi-threaded HTTP Server**:
  - Uses `com.sun.net.httpserver.HttpServer` with non-blocking worker pools.
  - REST API router dispatching `/api/auth/*`, `/api/overview`, `/api/subjects`, `/api/attendance`, `/api/calendar`, `/api/events`, `/api/marks`, `/api/journal`, `/api/profile`, `/api/admin/stats`, `/api/search`.
  - Serves static assets (`index.html`, `style.css`, `api.js`, `app.js`).

---

## 🚀 Running the Project

### Windows 1-Click Launch:
Simply run:
```bat
run.bat
```
This script automatically compiles all Java classes into `bin/`, starts the server on port `8080`, and opens `http://localhost:8080/` in your default browser.

### Manual Compile and Run:
```bat
build.bat
java -cp "bin;lib/h2.jar" edu.mit.academicclock.Main
```

### Running Test Suite:
```bash
node test-all.js
```
Runs an automated 11-step integration test validating registration, authentication, database persistence, Streams API queries, and timetable lookups.

---

## ⚡ Deploying to Vercel (1-Click Cloud Deployment)

ClockIt is fully optimized for **Vercel** with a zero-configuration serverless setup (`vercel.json` and `api/index.js`):

1. Go to **[vercel.com](https://vercel.com)** and sign in with your GitHub account.
2. Click **"Add New..."** → **"Project"**.
3. Import the repository **`a-priyanthi/ClockIt4`**.
4. Leave the default settings:
   - **Framework Preset**: *Other*
   - **Root Directory**: `./`
   - **Build Command**: *None*
   - **Output Directory**: *public* (or default)
5. Click **"Deploy"**.

Within seconds, your live production URL will be ready, hosting the complete interactive ClockIt web platform, authentication portal, Anna University Semester 3 syllabus tracker, and official November 2026 End-Sem exam countdown!

---

## 📚 Curriculum Subjects Supported (Semester III CSE/CT)
1. `MA23C05` — Probability and Statistics (4 credits)
2. `CS23301` — Software Engineering (3 credits)
3. `CS23302` — Data Structures (5 credits)
4. `CS23303` — Digital System Design (5 credits)
5. `CS23304` — Java Programming (5 credits)
6. `CS23U01` — Standards - Computer Science & Engg. (1 credit)
7. `UC23U01` — Universal Human Values (2 credits)
