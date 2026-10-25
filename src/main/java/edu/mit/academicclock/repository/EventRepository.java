package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.CampusEvent;

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
public class EventRepository implements CrudRepository<CampusEvent, String> {

    @Override
    public CampusEvent save(CampusEvent evt) {
        if (evt.getId() == null || evt.getId().isBlank()) {
            evt.setId("evt-" + UUID.randomUUID().toString().substring(0, 8));
        }

        String sql = """
            MERGE INTO campus_events (id, title, category, event_date, event_time, venue, description, is_registered, updated_at)
            KEY(id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, evt.getId());
            ps.setString(2, evt.getTitle());
            ps.setString(3, evt.getCategory());
            ps.setDate(4, Date.valueOf(evt.getDate()));
            ps.setString(5, evt.getTime());
            ps.setString(6, evt.getPlace());
            ps.setString(7, evt.getDescription());
            ps.setBoolean(8, evt.isRegistered());
            ps.setTimestamp(9, Timestamp.valueOf(evt.getUpdatedAt()));
            ps.executeUpdate();
            return evt;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save event: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CampusEvent> findById(String id) {
        String sql = "SELECT * FROM campus_events WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find event by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CampusEvent> findAll() {
        List<CampusEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM campus_events ORDER BY event_date ASC, event_time ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch campus events", e);
        }
        return list;
    }

    public boolean toggleRegistration(String id) {
        String sql = "UPDATE campus_events SET is_registered = NOT is_registered, updated_at = CURRENT_TIMESTAMP WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to toggle registration for event: " + id, e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM campus_events WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete event: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM campus_events;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count events", e);
        }
        return 0;
    }

    private CampusEvent mapRow(ResultSet rs) throws SQLException {
        CampusEvent evt = new CampusEvent();
        evt.setId(rs.getString("id"));
        evt.setTitle(rs.getString("title"));
        evt.setCategory(rs.getString("category"));
        evt.setDate(rs.getDate("event_date").toLocalDate());
        evt.setTime(rs.getString("event_time"));
        evt.setPlace(rs.getString("venue"));
        evt.setDescription(rs.getString("description"));
        evt.setRegistered(rs.getBoolean("is_registered"));
        evt.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        evt.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return evt;
    }
}
