package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Unit IV: Databases with JDBC & Generic CrudRepository
 */
public class SubjectRepository implements CrudRepository<Subject, String> {

    @Override
    public Subject save(Subject s) {
        if (s.getId() == null || s.getId().isBlank()) {
            Optional<Subject> existing = findByCode(s.getCode());
            if (existing.isPresent()) {
                s.setId(existing.get().getId());
                if (s.getAttended() == 0 && s.getTotal() == 0) {
                    s.setAttended(existing.get().getAttended());
                    s.setTotal(existing.get().getTotal());
                }
            } else {
                s.setId("sub-" + UUID.randomUUID().toString().substring(0, 8));
            }
        }

        String sql = """
            MERGE INTO subjects (id, code, name, units, progress, attended, total, updated_at)
            KEY(id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getId());
            ps.setString(2, s.getCode());
            ps.setString(3, s.getName());
            ps.setInt(4, s.getUnits());
            ps.setInt(5, s.getProgress());
            ps.setInt(6, s.getAttended());
            ps.setInt(7, s.getTotal());
            ps.setTimestamp(8, Timestamp.valueOf(s.getUpdatedAt()));
            ps.executeUpdate();
            return s;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save subject " + s.getCode() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Subject> findById(String id) {
        String sql = "SELECT * FROM subjects WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch subject by id: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Subject> findByCode(String code) {
        String sql = "SELECT * FROM subjects WHERE UPPER(code) = UPPER(?);";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch subject by code: " + code, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY code ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch all subjects: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateProgress(String code, int units, int progress) {
        String sql = "UPDATE subjects SET units = ?, progress = ?, updated_at = CURRENT_TIMESTAMP WHERE UPPER(code) = UPPER(?);";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, units);
            ps.setInt(2, progress);
            ps.setString(3, code.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update progress for " + code, e);
        }
    }

    public boolean recordAttendance(String code, boolean present) {
        String sql = "UPDATE subjects SET total = total + 1, attended = attended + ?, updated_at = CURRENT_TIMESTAMP WHERE UPPER(code) = UPPER(?);";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, present ? 1 : 0);
            ps.setString(2, code.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to record attendance for " + code, e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM subjects WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete subject: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM subjects;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count subjects", e);
        }
        return 0;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getString("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setUnits(rs.getInt("units"));
        s.setProgress(rs.getInt("progress"));
        s.setAttended(rs.getInt("attended"));
        s.setTotal(rs.getInt("total"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return s;
    }
}
