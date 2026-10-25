package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.JournalEntry;

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
public class JournalRepository implements CrudRepository<JournalEntry, String> {

    @Override
    public JournalEntry save(JournalEntry j) {
        if (j.getId() == null || j.getId().isBlank()) {
            j.setId("jrn-" + UUID.randomUUID().toString().substring(0, 8));
        }

        String sql = """
            MERGE INTO journal_entries (id, title, content, mood, entry_date, updated_at)
            KEY(id)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, j.getId());
            ps.setString(2, j.getTitle());
            ps.setString(3, j.getText());
            ps.setString(4, j.getMood());
            ps.setDate(5, Date.valueOf(j.getEntryDate()));
            ps.setTimestamp(6, Timestamp.valueOf(j.getUpdatedAt()));
            ps.executeUpdate();
            return j;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save journal entry: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<JournalEntry> findById(String id) {
        String sql = "SELECT * FROM journal_entries WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find journal entry by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<JournalEntry> findAll() {
        List<JournalEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM journal_entries ORDER BY entry_date DESC, created_at DESC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch journal entries", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM journal_entries WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete journal entry: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM journal_entries;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count journal entries", e);
        }
        return 0;
    }

    private JournalEntry mapRow(ResultSet rs) throws SQLException {
        JournalEntry j = new JournalEntry();
        j.setId(rs.getString("id"));
        j.setTitle(rs.getString("title"));
        j.setText(rs.getString("content"));
        j.setMood(rs.getString("mood"));
        j.setEntryDate(rs.getDate("entry_date").toLocalDate());
        j.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        j.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return j;
    }
}
