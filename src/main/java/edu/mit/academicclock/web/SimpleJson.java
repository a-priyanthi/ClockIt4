package edu.mit.academicclock.web;

import edu.mit.academicclock.model.AssessmentMark;
import edu.mit.academicclock.model.CalendarItem;
import edu.mit.academicclock.model.CampusEvent;
import edu.mit.academicclock.model.JournalEntry;
import edu.mit.academicclock.model.Student;
import edu.mit.academicclock.model.Subject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit I: String Class & Regular Expressions (Unit IV)
 * Lightweight zero-dependency JSON serializer and parser for Java HTTP Server.
 */
public class SimpleJson {

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof LocalDate || obj instanceof LocalDateTime) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        // Domain models reflection / mapping
        if (obj instanceof Subject) {
            Subject s = (Subject) obj;
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("code", s.getCode());
            m.put("name", s.getName());
            m.put("units", s.getUnits());
            m.put("progress", s.getProgress());
            m.put("attended", s.getAttended());
            m.put("total", s.getTotal());
            m.put("attendancePercentage", s.getAttendancePercentage());
            return toJson(m);
        }
        if (obj instanceof CalendarItem) {
            CalendarItem c = (CalendarItem) obj;
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("title", c.getTitle());
            m.put("date", c.getDate().toString());
            m.put("type", c.getType());
            m.put("detail", c.getDetail());
            return toJson(m);
        }
        if (obj instanceof CampusEvent) {
            CampusEvent e = (CampusEvent) obj;
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("category", e.getCategory());
            m.put("date", e.getDate().toString());
            m.put("time", e.getTime());
            m.put("place", e.getPlace());
            m.put("description", e.getDescription());
            m.put("registered", e.isRegistered());
            return toJson(m);
        }
        if (obj instanceof AssessmentMark) {
            AssessmentMark mark = (AssessmentMark) obj;
            Map<String, Object> m = new HashMap<>();
            m.put("id", mark.getId());
            m.put("subject", mark.getSubject());
            m.put("assessment", mark.getAssessment());
            m.put("score", mark.getScore());
            m.put("outof", mark.getOutOf());
            return toJson(m);
        }
        if (obj instanceof JournalEntry) {
            JournalEntry j = (JournalEntry) obj;
            Map<String, Object> m = new HashMap<>();
            m.put("id", j.getId());
            m.put("title", j.getTitle());
            m.put("text", j.getText());
            m.put("mood", j.getMood());
            m.put("date", j.getEntryDate().toString());
            return toJson(m);
        }
        if (obj instanceof Student) {
            Student s = (Student) obj;
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("rollNumber", s.getRollNumber());
            m.put("name", s.getName());
            m.put("department", s.getDepartment());
            m.put("programme", s.getProgramme());
            m.put("semester", s.getSemester());
            m.put("avatarUrl", s.getAvatarUrl());
            m.put("doodleApplied", s.isDoodleApplied());
            return toJson(m);
        }

        return "\"" + escape(obj.toString()) + "\"";
    }

    /**
     * Parses simple JSON flat object string into Map<String, String>.
     * Unit IV: Regular Expressions
     */
    /**
     * Parses simple JSON object string into Map<String, String>.
     * Unit IV: String parsing and error-resilient tokenization
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return map;
        }

        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        // Tokenize key-value pairs respecting quotes
        int len = trimmed.length();
        int i = 0;
        while (i < len) {
            // Skip whitespace and commas
            while (i < len && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ',')) i++;
            if (i >= len) break;

            // Read Key
            String key = "";
            if (trimmed.charAt(i) == '"' || trimmed.charAt(i) == '\'') {
                char quote = trimmed.charAt(i++);
                StringBuilder sb = new StringBuilder();
                while (i < len && trimmed.charAt(i) != quote) {
                    if (trimmed.charAt(i) == '\\' && i + 1 < len) i++;
                    sb.append(trimmed.charAt(i++));
                }
                if (i < len) i++; // skip closing quote
                key = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                while (i < len && trimmed.charAt(i) != ':' && !Character.isWhitespace(trimmed.charAt(i))) {
                    sb.append(trimmed.charAt(i++));
                }
                key = sb.toString();
            }

            // Skip until colon
            while (i < len && trimmed.charAt(i) != ':') i++;
            if (i < len && trimmed.charAt(i) == ':') i++;
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) i++;
            if (i >= len) break;

            // Read Value
            String val = "";
            if (trimmed.charAt(i) == '"' || trimmed.charAt(i) == '\'') {
                char quote = trimmed.charAt(i++);
                StringBuilder sb = new StringBuilder();
                while (i < len && trimmed.charAt(i) != quote) {
                    if (trimmed.charAt(i) == '\\' && i + 1 < len) {
                        char next = trimmed.charAt(++i);
                        if (next == 'n') sb.append('\n');
                        else if (next == 'r') sb.append('\r');
                        else if (next == 't') sb.append('\t');
                        else sb.append(next);
                        i++;
                        continue;
                    }
                    sb.append(trimmed.charAt(i++));
                }
                if (i < len) i++; // skip closing quote
                val = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') {
                    sb.append(trimmed.charAt(i++));
                }
                val = sb.toString().trim();
                if (val.equalsIgnoreCase("null")) val = "";
            }

            if (!key.isEmpty()) {
                map.put(key.trim(), val);
            }
        }

        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
