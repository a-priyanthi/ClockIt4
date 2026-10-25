package edu.mit.academicclock.service;

import edu.mit.academicclock.exception.ValidationException;
import edu.mit.academicclock.model.AssessmentMark;
import edu.mit.academicclock.model.CalendarItem;
import edu.mit.academicclock.model.CampusEvent;
import edu.mit.academicclock.model.JournalEntry;
import edu.mit.academicclock.model.Student;
import edu.mit.academicclock.model.Subject;
import edu.mit.academicclock.repository.AttendanceRepository;
import edu.mit.academicclock.repository.CalendarRepository;
import edu.mit.academicclock.repository.EventRepository;
import edu.mit.academicclock.repository.JournalRepository;
import edu.mit.academicclock.repository.MarksRepository;
import edu.mit.academicclock.repository.StudentRepository;
import edu.mit.academicclock.repository.SubjectRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Unit IV: Streams API & Lambda Expressions
 * Unit I: Working with Date & Time API
 * Service coordinating domain business logic and Streams transformations.
 */
public class AcademicService {
    private final SubjectRepository subjectRepo;
    private final CalendarRepository calendarRepo;
    private final EventRepository eventRepo;
    private final MarksRepository marksRepo;
    private final JournalRepository journalRepo;
    private final StudentRepository studentRepo;
    private final AttendanceRepository attendanceRepo;

    public AcademicService(
            SubjectRepository subjectRepo,
            CalendarRepository calendarRepo,
            EventRepository eventRepo,
            MarksRepository marksRepo,
            JournalRepository journalRepo,
            StudentRepository studentRepo,
            AttendanceRepository attendanceRepo) {
        this.subjectRepo = subjectRepo;
        this.calendarRepo = calendarRepo;
        this.eventRepo = eventRepo;
        this.marksRepo = marksRepo;
        this.journalRepo = journalRepo;
        this.studentRepo = studentRepo;
        this.attendanceRepo = attendanceRepo;
    }

    // 1. Dashboard Overview using Streams API & Lambdas (Unit IV)
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();

        List<Subject> allSubjects = subjectRepo.findAll();
        List<CalendarItem> allCalendarItems = calendarRepo.findAll();
        List<JournalEntry> allJournals = journalRepo.findAll();

        // Calculate Average Attendance percentage with Streams API (Unit IV)
        double avgAttendance = allSubjects.stream()
                .filter(s -> s.getTotal() > 0)
                .mapToDouble(s -> ((double) s.getAttended() / s.getTotal()) * 100.0)
                .average()
                .orElse(85.0);

        // Filter and sort upcoming deadlines with Streams API & Lambdas (Unit IV)
        LocalDate baseline = LocalDate.of(2026, 10, 20); // demo reference date
        List<CalendarItem> upcomingDeadlines = allCalendarItems.stream()
                .filter(item -> !item.getDate().isBefore(baseline))
                .sorted(Comparator.comparing(CalendarItem::getDate))
                .limit(4)
                .collect(Collectors.toList());

        overview.put("averageAttendance", Math.round(avgAttendance));
        overview.put("upcomingCount", upcomingDeadlines.size());
        overview.put("journalCount", allJournals.size());
        overview.put("deadlines", upcomingDeadlines);
        overview.put("totalSubjects", allSubjects.size());
        overview.put("totalEvents", eventRepo.count());

        return overview;
    }

    // 2. Global Search using Streams API and Lambdas (Unit IV)
    public Map<String, Object> search(String query) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Search query cannot be empty");
        }
        String q = query.trim().toLowerCase();
        Map<String, Object> results = new HashMap<>();

        List<Subject> matchedSubjects = subjectRepo.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(q) || s.getCode().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<CampusEvent> matchedEvents = eventRepo.findAll().stream()
                .filter(e -> e.getTitle().toLowerCase().contains(q) || e.getDescription().toLowerCase().contains(q) || e.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<CalendarItem> matchedCalendar = calendarRepo.findAll().stream()
                .filter(c -> c.getTitle().toLowerCase().contains(q) || (c.getDetail() != null && c.getDetail().toLowerCase().contains(q)))
                .collect(Collectors.toList());

        results.put("query", query);
        results.put("subjects", matchedSubjects);
        results.put("events", matchedEvents);
        results.put("calendarItems", matchedCalendar);

        return results;
    }

    // 3. Subject & Attendance Operations
    public List<Subject> getAllSubjects() {
        return subjectRepo.findAll();
    }

    public Subject saveSubject(Subject s) {
        if (s.getCode() == null || s.getCode().isBlank()) {
            throw new ValidationException("Subject code is required");
        }
        if (s.getName() == null || s.getName().isBlank()) {
            throw new ValidationException("Subject name is required");
        }
        return subjectRepo.save(s);
    }

    public boolean updateSubject(String code, int units, int progress) {
        return subjectRepo.updateProgress(code, units, progress);
    }

    public boolean recordAttendance(String subjectCode, String status, LocalDate date) {
        if (subjectCode == null || subjectCode.isBlank()) {
            throw new ValidationException("Subject code cannot be blank");
        }
        boolean isPresent = "present".equalsIgnoreCase(status);
        attendanceRepo.save(new edu.mit.academicclock.model.AttendanceRecord(null, subjectCode, date != null ? date : LocalDate.now(), status));
        return subjectRepo.recordAttendance(subjectCode, isPresent);
    }

    // 4. Calendar Operations
    public List<CalendarItem> getAllCalendarItems() {
        return calendarRepo.findAll();
    }

    public List<CalendarItem> getCalendarItemsForDate(LocalDate date) {
        return calendarRepo.findByDate(date);
    }

    public CalendarItem addCalendarItem(CalendarItem item) {
        if (item.getTitle() == null || item.getTitle().isBlank()) {
            throw new ValidationException("Calendar item title is required");
        }
        if (item.getDate() == null) {
            throw new ValidationException("Calendar item date is required");
        }
        return calendarRepo.save(item);
    }

    // 5. Campus Events Operations
    public List<CampusEvent> getAllEvents() {
        return eventRepo.findAll();
    }

    public CampusEvent addEvent(CampusEvent event) {
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new ValidationException("Event title is required");
        }
        return eventRepo.save(event);
    }

    public boolean toggleEventRegistration(String eventId) {
        return eventRepo.toggleRegistration(eventId);
    }

    // 6. Assessment Marks Operations
    public List<AssessmentMark> getAllMarks() {
        return marksRepo.findAll();
    }

    public AssessmentMark addMark(AssessmentMark mark) {
        if (mark.getSubject() == null || mark.getSubject().isBlank()) {
            throw new ValidationException("Subject name is required");
        }
        if (mark.getAssessment() == null || mark.getAssessment().isBlank()) {
            throw new ValidationException("Assessment name is required");
        }
        return marksRepo.save(mark);
    }

    public boolean deleteMark(String id) {
        return marksRepo.deleteById(id);
    }

    // 7. Journal Operations
    public List<JournalEntry> getAllJournalEntries() {
        return journalRepo.findAll();
    }

    public JournalEntry addJournalEntry(JournalEntry entry) {
        if (entry.getTitle() == null || entry.getTitle().isBlank()) {
            entry.setTitle("A small memory");
        }
        return journalRepo.save(entry);
    }

    // 8. Student Profile Operations
    public Optional<Student> getStudentProfile() {
        return studentRepo.getPrimaryStudent();
    }

    public Student updateStudentProfile(Student updated) {
        Optional<Student> current = studentRepo.getPrimaryStudent();
        if (current.isPresent()) {
            Student s = current.get();
            if (updated.getName() != null && !updated.getName().isBlank()) s.setName(updated.getName());
            if (updated.getRollNumber() != null && !updated.getRollNumber().isBlank()) s.setRollNumber(updated.getRollNumber());
            if (updated.getDepartment() != null) s.setDepartment(updated.getDepartment());
            if (updated.getProgramme() != null) s.setProgramme(updated.getProgramme());
            if (updated.getSemester() > 0) s.setSemester(updated.getSemester());
            if (updated.getAvatarUrl() != null) s.setAvatarUrl(updated.getAvatarUrl());
            s.setDoodleApplied(updated.isDoodleApplied());
            return studentRepo.save(s);
        } else {
            return studentRepo.save(updated);
        }
    }

    // 9. Admin Stats
    public Map<String, Long> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("calendarItems", calendarRepo.count());
        stats.put("events", eventRepo.count());
        stats.put("subjects", subjectRepo.count());
        stats.put("marks", marksRepo.count());
        stats.put("journals", journalRepo.count());
        return stats;
    }
}
