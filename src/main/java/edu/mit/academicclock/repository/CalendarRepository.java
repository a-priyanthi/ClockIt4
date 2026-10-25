package edu.mit.academicclock.repository;

import edu.mit.academicclock.config.DatabaseConfig;
import edu.mit.academicclock.exception.DatabaseOperationException;
import edu.mit.academicclock.model.CalendarItem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Unit IV: Databases with JDBC
 */
public class CalendarRepository implements CrudRepository<CalendarItem, String> {

    @Override
    public CalendarItem save(CalendarItem item) {
        if (item.getId() == null || item.getId().isBlank()) {
            item.setId("cal-" + UUID.randomUUID().toString().substring(0, 8));
        }

        String sql = """
            MERGE INTO calendar_items (id, title, item_date, item_type, detail, updated_at)
            KEY(id)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getId());
            ps.setString(2, item.getTitle());
            ps.setDate(3, Date.valueOf(item.getDate()));
            ps.setString(4, item.getType());
            ps.setString(5, item.getDetail());
            ps.setTimestamp(6, Timestamp.valueOf(item.getUpdatedAt()));
            ps.executeUpdate();
            return item;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save calendar item: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CalendarItem> findById(String id) {
        String sql = "SELECT * FROM calendar_items WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find calendar item by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CalendarItem> findAll() {
        List<CalendarItem> list = new ArrayList<>();
        String sql = "SELECT * FROM calendar_items ORDER BY item_date ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch calendar items", e);
        }
        return list;
    }

    // Method Overloading for Unit II
    public List<CalendarItem> findByDate(LocalDate date) {
        List<CalendarItem> list = new ArrayList<>();
        String sql = "SELECT * FROM calendar_items WHERE item_date = ? ORDER BY title ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query calendar items for date: " + date, e);
        }
        return list;
    }

    // Method Overloading for Unit II
    public List<CalendarItem> findByDateRange(LocalDate start, LocalDate end) {
        List<CalendarItem> list = new ArrayList<>();
        String sql = "SELECT * FROM calendar_items WHERE item_date BETWEEN ? AND ? ORDER BY item_date ASC;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query calendar items between dates", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM calendar_items WHERE id = ?;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete calendar item: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM calendar_items;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count calendar items", e);
        }
        return 0;
    }

    private CalendarItem mapRow(ResultSet rs) throws SQLException {
        CalendarItem c = new CalendarItem();
        c.setId(rs.getString("id"));
        c.setTitle(rs.getString("title"));
        c.setDate(rs.getDate("item_date").toLocalDate());
        c.setType(rs.getString("item_type"));
        c.setDetail(rs.getString("detail"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    }
}
