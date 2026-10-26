package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Unit IV: Databases with JDBC
 */
public class StudentRepository implements CrudRepository<Student, String> {

    @Override
    public Student save(Student s) {
        if (s.getId() == null || s.getId().isBlank()) {
            Optional<Student> existing = findByRollNumber(s.getRollNumber());
            if (existing.isPresent()) {
                s.setId(existing.get().getId());
            } else {
                s.setId("std-" + System.currentTimeMillis());
            }
        }

        String sql = """
            MERGE INTO students (id, roll_number, name, department, programme, semester, gender, password, avatar_url, is_doodle, updated_at)
            KEY(id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getId());
            ps.setString(2, s.getRollNumber());
            ps.setString(3, s.getName());
            ps.setString(4, s.getDepartment());
            ps.setString(5, s.getProgramme() != null ? s.getProgramme() : "B.E. Computer Science and Engineering");
            ps.setInt(6, s.getSemester() > 0 ? s.getSemester() : 3);
            ps.setString(7, s.getGender() != null ? s.getGender() : "Female");
            ps.setString(8, s.getPassword() != null ? s.getPassword() : "clockit2026");
            ps.setString(9, s.getAvatarUrl());
            ps.setBoolean(10, s.isDoodleApplied());
            ps.setTimestamp(11, Timestamp.valueOf(s.getUpdatedAt()));
            ps.executeUpdate();
            return s;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save student profile: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> findById(String id) {
        String sql = "SELECT * FROM students WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find student by id: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Student> findByRollNumber(String roll) {
        String sql = "SELECT * FROM students WHERE UPPER(roll_number) = UPPER(?);";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find student by roll number: " + roll, e);
        }
        return Optional.empty();
    }

    public Optional<Student> authenticate(String roll, String password) {
        if (roll == null || roll.isBlank()) return Optional.empty();
        Optional<Student> studentOpt = findByRollNumber(roll.trim());
        if (studentOpt.isEmpty()) return Optional.empty();

        Student s = studentOpt.get();
        // If password is not set or matches input
        if (s.getPassword() == null || s.getPassword().isBlank() || s.getPassword().equals(password != null ? password.trim() : "")) {
            return Optional.of(s);
        }
        return Optional.empty();
    }

    public Optional<Student> getPrimaryStudent() {
        String sql = "SELECT * FROM students ORDER BY created_at ASC LIMIT 1;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get primary student profile", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_number ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch students", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM students WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete student: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM students;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count students", e);
        }
        return 0;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getString("id"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setName(rs.getString("name"));
        s.setDepartment(rs.getString("department"));
        s.setProgramme(rs.getString("programme"));
        s.setSemester(rs.getInt("semester"));
        
        try {
            s.setGender(rs.getString("gender"));
        } catch (SQLException ignored) {
            s.setGender("Female");
        }

        try {
            s.setPassword(rs.getString("password"));
        } catch (SQLException ignored) {
            s.setPassword("clockit2026");
        }

        s.setAvatarUrl(rs.getString("avatar_url"));
        s.setDoodleApplied(rs.getBoolean("is_doodle"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return s;
    }
}
