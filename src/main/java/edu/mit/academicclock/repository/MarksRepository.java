package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.AssessmentMark;

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
 * Unit IV: Databases with JDBC
 */
public class MarksRepository implements CrudRepository<AssessmentMark, String> {

    @Override
    public AssessmentMark save(AssessmentMark m) {
        if (m.getId() == null || m.getId().isBlank()) {
            m.setId("mrk-" + UUID.randomUUID().toString().substring(0, 8));
        }

        String sql = """
            MERGE INTO assessment_marks (id, subject, assessment, score, out_of, updated_at)
            KEY(id)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getId());
            ps.setString(2, m.getSubject());
            ps.setString(3, m.getAssessment());
            ps.setDouble(4, m.getScore());
            ps.setDouble(5, m.getOutOf());
            ps.setTimestamp(6, Timestamp.valueOf(m.getUpdatedAt()));
            ps.executeUpdate();
            return m;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save assessment mark: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AssessmentMark> findById(String id) {
        String sql = "SELECT * FROM assessment_marks WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find mark: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<AssessmentMark> findAll() {
        List<AssessmentMark> list = new ArrayList<>();
        String sql = "SELECT * FROM assessment_marks ORDER BY subject ASC, assessment ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch assessment marks", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM assessment_marks WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete mark: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM assessment_marks;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count marks", e);
        }
        return 0;
    }

    private AssessmentMark mapRow(ResultSet rs) throws SQLException {
        AssessmentMark m = new AssessmentMark();
        m.setId(rs.getString("id"));
        m.setSubject(rs.getString("subject"));
        m.setAssessment(rs.getString("assessment"));
        m.setScore(rs.getDouble("score"));
        m.setOutOf(rs.getDouble("out_of"));
        m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        m.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return m;
    }
}
