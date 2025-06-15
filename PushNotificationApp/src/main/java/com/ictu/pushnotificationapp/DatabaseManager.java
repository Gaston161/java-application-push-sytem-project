// =================================================================================
// Fichier : DatabaseManager.java (Final)
package com.ictu.pushnotificationapp;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;



public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:database.db";

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.createStatement().execute("PRAGMA foreign_keys = ON;");
        return conn;
    }

    public static int validateUserAndGetId(String u, String p) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u); ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error validateUser", e); return -1; }
    }

    public static void addContact(String name, String phone, String email) {
        String sql = "INSERT OR IGNORE INTO contacts(user_id, name, phone, email) VALUES(?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error addContact", e); }
    }

    public static void loadContactsIntoTable(DefaultTableModel m) {
        m.setRowCount(0);
        String sql = "SELECT contact_id, name, phone, email FROM contacts WHERE user_id = ? ORDER BY name COLLATE NOCASE";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m.addRow(new Object[]{rs.getInt("contact_id"), rs.getString("name"),
                    rs.getString("phone"), rs.getString("email"), false});
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error loadContacts", e); }
    }

    public static void deleteContact(int id) {
        String sql = "DELETE FROM contacts WHERE contact_id = ? AND user_id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setInt(2, UserSession.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error deleteContact", e); }
    }
    
    public static void addLogEntry(String group, String recipient, String msg, String status, String details) {
        String sql = "INSERT INTO send_history(user_id, recipient_group, recipient, message, send_time, status, status_details) VALUES(?,?,?,?,datetime('now', 'localtime'),?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ps.setString(2, group);
            ps.setString(3, recipient);
            ps.setString(4, msg);
            ps.setString(5, status);
            ps.setString(6, details);
            ps.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error addLogEntry", e); }
    }

    public static void loadHistoryIntoTable(DefaultTableModel m) {
        m.setRowCount(0);
        String sql = "SELECT send_time, recipient_group, recipient, message, status, status_details FROM send_history WHERE user_id = ? ORDER BY send_time DESC";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m.addRow(new Object[]{rs.getString("send_time"), rs.getString("recipient_group"), rs.getString("recipient"),
                    rs.getString("message"), rs.getString("status"), rs.getString("status_details")});
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error loadHistory", e); }
    }

    public static int[] getDashboardStats() {
        int[] stats = {0, 0, 0};
        String sql = "SELECT status, COUNT(*) as count FROM send_history WHERE user_id = ? GROUP BY status";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if ("Succès".equals(rs.getString("status"))) stats[1] = rs.getInt("count");
                else stats[2] += rs.getInt("count");
            }
            stats[0] = stats[1] + stats[2];
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error getDashboardStats", e); }
        return stats;
    }
    
    // --- Group Management ---
    public static void createGroup(String groupName) {
        String sql = "INSERT OR IGNORE INTO contact_groups(user_id, group_name) VALUES(?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ps.setString(2, groupName);
            ps.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error createGroup", e); }
    }

    public static List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT group_name FROM contact_groups WHERE user_id = ? ORDER BY group_name COLLATE NOCASE";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                groups.add(rs.getString("group_name"));
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error getGroups", e); }
        return groups;
    }
    
    public static int getGroupId(String groupName) {
        String sql = "SELECT group_id FROM contact_groups WHERE user_id = ? AND group_name = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ps.setString(2, groupName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("group_id");
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error getGroupId", e); }
        return -1;
    }

    public static void addContactsToGroup(List<Integer> contactIds, int groupId) {
        String sql = "INSERT OR IGNORE INTO contact_group_members(group_id, contact_id) VALUES(?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (Integer contactId : contactIds) {
                ps.setInt(1, groupId);
                ps.setInt(2, contactId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error addContactsToGroup", e); }
    }
    
    public static List<JSONObject> getContactsByGroup(String groupName) {
        List<JSONObject> contacts = new ArrayList<>();
        String sql = "SELECT c.name, c.phone, c.email FROM contacts c JOIN contact_group_members cgm ON c.contact_id = cgm.contact_id JOIN contact_groups cg ON cgm.group_id = cg.group_id WHERE cg.user_id = ? AND cg.group_name = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ps.setString(2, groupName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject contact = new JSONObject();
                contact.put("name", rs.getString("name"));
                contact.put("phone", rs.getString("phone"));
                contact.put("email", rs.getString("email"));
                contacts.add(contact);
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error getContactsByGroup", e); }
        return contacts;
    }
    
    // --- Scheduler Management ---
    public static void scheduleSend(String type, String recipientType, String recipientName, String subject, String message, String attachmentPath, LocalDateTime dateTime) {
        String sql = "INSERT INTO scheduled_sends(user_id, send_type, recipient_type, recipient_name, subject, message, attachment_path, scheduled_time, status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ps.setString(2, type);
            ps.setString(3, recipientType);
            ps.setString(4, recipientName);
            ps.setString(5, subject);
            ps.setString(6, message);
            ps.setString(7, attachmentPath);
            ps.setString(8, dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setString(9, "En attente");
            ps.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error scheduleSend", e); }
    }

    public static List<JSONObject> getPendingScheduledSends() {
        List<JSONObject> tasks = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_sends WHERE status = 'En attente' AND scheduled_time <= datetime('now', 'localtime')";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                JSONObject task = new JSONObject();
                task.put("schedule_id", rs.getInt("schedule_id"));
                task.put("user_id", rs.getInt("user_id"));
                task.put("send_type", rs.getString("send_type"));
                task.put("recipient_type", rs.getString("recipient_type"));
                task.put("recipient_name", rs.getString("recipient_name"));
                task.put("subject", rs.getString("subject"));
                task.put("message", rs.getString("message"));
                task.put("attachment_path", rs.getString("attachment_path"));
                tasks.add(task);
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error getPendingScheduledSends", e); }
        return tasks;
    }
    
    public static void loadScheduledIntoTable(DefaultTableModel m) {
        m.setRowCount(0);
        String sql = "SELECT schedule_id, scheduled_time, send_type, recipient_type, recipient_name, status FROM scheduled_sends WHERE user_id = ? ORDER BY scheduled_time DESC";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, UserSession.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m.addRow(new Object[]{rs.getInt("schedule_id"), rs.getString("scheduled_time"), 
                    rs.getString("send_type"), rs.getString("recipient_type"), rs.getString("recipient_name"), rs.getString("status")});
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error loadScheduledIntoTable", e); }
    }

    public static void updateScheduledSendStatus(int id, String status) {
        String sql = "UPDATE scheduled_sends SET status = ? WHERE schedule_id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "DB Error updateScheduledSendStatus", e); }
    }
}
