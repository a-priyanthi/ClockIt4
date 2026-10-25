package edu.mit.academicclock.web;

import com.sun.net.httpserver.HttpExchange;
import edu.mit.academicclock.exception.AcademicClockException;
import edu.mit.academicclock.exception.ValidationException;
import edu.mit.academicclock.model.AssessmentMark;
import edu.mit.academicclock.model.CalendarItem;
import edu.mit.academicclock.model.CampusEvent;
import edu.mit.academicclock.model.JournalEntry;
import edu.mit.academicclock.model.Student;
import edu.mit.academicclock.model.Subject;
import edu.mit.academicclock.service.AcademicService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit V: Web Development and Frameworks
 * Unit III: Exception Handling
 * REST Endpoint Handlers communicating between HTTP requests and the Service layer.
 */
public class ApiHandlers {
    private final AcademicService service;

    public ApiHandlers(AcademicService service) {
        this.service = service;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        // Handle CORS Pre-flight options
        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            // Route dispatching
            if (path.equals("/api/overview") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getDashboardOverview());
            } else if (path.equals("/api/subjects") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getAllSubjects());
            } else if (path.equals("/api/subjects") && method.equals("POST")) {
                handleCreateSubject(exchange);
            } else if (path.startsWith("/api/subjects/") && (method.equals("PUT") || method.equals("POST"))) {
                handleUpdateSubject(exchange, path.substring("/api/subjects/".length()));
            } else if (path.equals("/api/attendance") && method.equals("POST")) {
                handleRecordAttendance(exchange);
            } else if (path.equals("/api/calendar") && method.equals("GET")) {
                handleGetCalendar(exchange);
            } else if (path.equals("/api/calendar") && method.equals("POST")) {
                handleCreateCalendar(exchange);
            } else if (path.startsWith("/api/calendar/") && method.equals("DELETE")) {
                handleDeleteCalendar(exchange, path.substring("/api/calendar/".length()));
            } else if (path.equals("/api/events") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getAllEvents());
            } else if (path.equals("/api/events") && method.equals("POST")) {
                handleCreateEvent(exchange);
            } else if (path.matches("/api/events/[^/]+/register") && method.equals("POST")) {
                String eventId = path.split("/")[3];
                boolean ok = service.toggleEventRegistration(eventId);
                Map<String, Object> res = new HashMap<>();
                res.put("success", ok);
                res.put("events", service.getAllEvents());
                sendJsonResponse(exchange, 200, res);
            } else if (path.equals("/api/marks") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getAllMarks());
            } else if (path.equals("/api/marks") && method.equals("POST")) {
                handleCreateMark(exchange);
            } else if (path.startsWith("/api/marks/") && method.equals("DELETE")) {
                handleDeleteMark(exchange, path.substring("/api/marks/".length()));
            } else if (path.equals("/api/journal") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getAllJournalEntries());
            } else if (path.equals("/api/journal") && method.equals("POST")) {
                handleCreateJournal(exchange);
            } else if (path.equals("/api/profile") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getStudentProfile().orElse(new Student()));
            } else if (path.equals("/api/profile") && method.equals("POST")) {
                handleUpdateProfile(exchange);
            } else if (path.equals("/api/admin/stats") && method.equals("GET")) {
                sendJsonResponse(exchange, 200, service.getAdminStats());
            } else if (path.equals("/api/search")) {
                handleSearch(exchange);
            } else {
                sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
            }

        } catch (AcademicClockException e) {
            sendErrorResponse(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleCreateSubject(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        Subject s = new Subject();
        s.setCode(data.get("code"));
        s.setName(data.get("name"));
        s.setUnits(parseInt(data.get("units"), 4));
        s.setProgress(parseInt(data.get("progress"), 0));
        s.setAttended(0);
        s.setTotal(0);

        Subject saved = service.saveSubject(s);
        sendJsonResponse(exchange, 201, saved);
    }

    private void handleUpdateSubject(HttpExchange exchange, String code) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        int units = parseInt(data.get("units"), 4);
        int progress = parseInt(data.get("progress"), 0);

        boolean updated = service.updateSubject(code, units, progress);
        Map<String, Object> res = new HashMap<>();
        res.put("success", updated);
        res.put("subjects", service.getAllSubjects());
        sendJsonResponse(exchange, 200, res);
    }

    private void handleRecordAttendance(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        String subject = data.get("subject");
        String status = data.get("status");

        boolean ok = service.recordAttendance(subject, status, LocalDate.now());


        Map<String, Object> res = new HashMap<>();
        res.put("success", ok);
        res.put("subjects", service.getAllSubjects());
        sendJsonResponse(exchange, 200, res);
    }

    private void handleGetCalendar(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("date=")) {
            String dateStr = query.split("date=")[1].split("&")[0];
            sendJsonResponse(exchange, 200, service.getCalendarItemsForDate(LocalDate.parse(dateStr)));
        } else {
            sendJsonResponse(exchange, 200, service.getAllCalendarItems());
        }
    }

    private void handleCreateCalendar(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        CalendarItem item = new CalendarItem();
        item.setTitle(data.get("title"));
        item.setDate(LocalDate.parse(data.get("date")));
        item.setType(data.getOrDefault("type", "deadline"));
        item.setDetail(data.getOrDefault("detail", "Personal reminder"));

        CalendarItem saved = service.addCalendarItem(item);
        sendJsonResponse(exchange, 201, saved);
    }

    private void handleDeleteCalendar(HttpExchange exchange, String id) throws IOException {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        sendJsonResponse(exchange, 200, res);
    }

    private void handleCreateEvent(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        CampusEvent evt = new CampusEvent();
        evt.setTitle(data.get("title"));
        evt.setCategory(data.getOrDefault("category", "CAMPUS LIFE"));
        evt.setDate(LocalDate.parse(data.get("date")));
        evt.setTime(data.getOrDefault("time", "2:00 PM"));
        evt.setPlace(data.getOrDefault("place", "Venue to be confirmed"));
        evt.setDescription(data.getOrDefault("description", ""));

        CampusEvent saved = service.addEvent(evt);

        // Also add corresponding calendar item for seamless academic clock wheel synchronization
        service.addCalendarItem(new CalendarItem(null, saved.getTitle(), saved.getDate(), "event", "Demo event · " + saved.getPlace()));

        sendJsonResponse(exchange, 201, saved);
    }

    private void handleCreateMark(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        AssessmentMark mark = new AssessmentMark();
        mark.setSubject(data.get("subject"));
        mark.setAssessment(data.get("assessment"));
        mark.setScore(parseDouble(data.get("score"), 0.0));
        mark.setOutOf(parseDouble(data.get("outof"), 20.0));

        AssessmentMark saved = service.addMark(mark);
        sendJsonResponse(exchange, 201, saved);
    }

    private void handleDeleteMark(HttpExchange exchange, String id) throws IOException {
        boolean ok = service.deleteMark(id);
        Map<String, Object> res = new HashMap<>();
        res.put("success", ok);
        res.put("marks", service.getAllMarks());
        sendJsonResponse(exchange, 200, res);
    }

    private void handleCreateJournal(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        JournalEntry j = new JournalEntry();
        j.setTitle(data.getOrDefault("title", "A good day, in little ways"));
        j.setText(data.getOrDefault("text", ""));
        j.setMood(data.getOrDefault("mood", "🙂"));
        j.setEntryDate(LocalDate.now());

        JournalEntry saved = service.addJournalEntry(j);
        sendJsonResponse(exchange, 201, saved);
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = SimpleJson.parseObject(body);
        Student s = new Student();
        s.setName(data.get("name"));
        s.setRollNumber(data.get("rollNumber"));
        s.setDepartment(data.get("department"));
        s.setProgramme(data.get("programme"));
        s.setSemester(parseInt(data.get("semester"), 1));
        s.setAvatarUrl(data.get("avatarUrl"));
        s.setDoodleApplied("true".equalsIgnoreCase(data.get("doodleApplied")));

        Student updated = service.updateStudentProfile(s);
        sendJsonResponse(exchange, 200, updated);
    }

    private void handleSearch(HttpExchange exchange) throws IOException {
        String query = "";
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> data = SimpleJson.parseObject(readBody(exchange));
            query = data.getOrDefault("query", "");
        } else {
            String q = exchange.getRequestURI().getQuery();
            if (q != null && q.contains("q=")) {
                query = q.split("q=")[1].split("&")[0];
            }
        }
        sendJsonResponse(exchange, 200, service.search(query));
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        sendCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = SimpleJson.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        Map<String, String> err = new HashMap<>();
        err.put("error", message);
        err.put("status", String.valueOf(statusCode));
        byte[] bytes = SimpleJson.toJson(err).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private int parseInt(String val, int def) {
        if (val == null || val.isBlank()) return def;
        try {
            return (int) Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private double parseDouble(String val, double def) {
        if (val == null || val.isBlank()) return def;
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
