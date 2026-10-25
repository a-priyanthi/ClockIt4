package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.AttendanceRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Unit IV: Databases with JDBC
 */
public class AttendanceRepository implements CrudRepository<AttendanceRecord, String> {

    @Override
    public AttendanceRecord save(AttendanceRecord rec) {
        if (rec.getId() == null || rec.getId().isBlank()) {
            rec.setId("att-" + UUID.randomUUID().toString().substring(0, 8));
        }

        String sql = """
            INSERT INTO attendance_records (id, subject_code, record_date, status, updated_at)
            VALUES (?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rec.getId());
            ps.setString(2, rec.getSubjectCode());
            ps.setDate(3, Date.valueOf(rec.getRecordDate()));
            ps.setString(4, rec.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(rec.getUpdatedAt()));
            ps.executeUpdate();
            return rec;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to log attendance record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AttendanceRecord> findById(String id) {
        String sql = "SELECT * FROM attendance_records WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find attendance record", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AttendanceRecord> findAll() {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records ORDER BY record_date DESC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch attendance history", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM attendance_records WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete attendance record", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM attendance_records;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count attendance records", e);
        }
        return 0;
    }

    private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getString("id"));
        r.setSubjectCode(rs.getString("subject_code"));
        r.setRecordDate(rs.getDate("record_date").toLocalDate());
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return r;
    }
}
