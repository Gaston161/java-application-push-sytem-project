package com.pushnotifier.service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour accéder à la base SQLite (login admin + historique).
 */
public class DatabaseService {
    private static final String JDBC_URL = "jdbc:sqlite:/home/gaston/Bureau/pushnotifier.db";
    // Remplacez "/chemin/complet/vers/pushnotifier.db" par le vrai chemin sur votre machine

    private Connection conn;
    private Object com;

    /** Ouvre la connexion à la base SQLite. */
    public DatabaseService() throws SQLException {
        conn = DriverManager.getConnection(JDBC_URL);
    }

    /**
     * Vérifie les identifiants d’un administrateur.
     * @param username le login saisi
     * @param plainPassword le mot de passe en clair (on le hash en SHA-256 pour comparer)
     * @return true si identifiants valides, false sinon
     */
    public boolean authenticateAdmin(String username, String plainPassword) {
        String sql = "SELECT COUNT(*) FROM admin WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Calcul du SHA-256 du mot de passe saisi
            String hashed = sha256(plainPassword);
            stmt.setString(1, username);
            stmt.setString(2, hashed);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Insère une entrée d’historique dans la table 'history'.
     * @param time date/heure d’envoi
     * @param channel "EMAIL" ou "SMS"
     * @param recipient destinataire (adresse e-mail ou numéro SMS)
     * @param success 1 si succès, 0 si échec
     * @param errorMsg message d’erreur (ou chaîne vide)
     */
    public void insertHistory(LocalDateTime time, String channel, String recipient, boolean success, String errorMsg) {
        String sql = "INSERT INTO history (send_time, channel, recipient, success, error_msg) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Format ISO 8601 pour SQLite (ex : 2025-06-03T14:30:00)
            String timestamp = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            stmt.setString(1, timestamp);
            stmt.setString(2, channel);
            stmt.setString(3, recipient);
            stmt.setInt(4, success ? 1 : 0);
            stmt.setString(5, errorMsg);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lit tout l’historique (liste d’objets HistoryEntry).
     * @return une liste de HistoryEntry
     */
    public List<com.pushnotifier.model.HistoryEntry> fetchAllHistory() {
        List<com.pushnotifier.model.HistoryEntry> list = new ArrayList<>();
        String sql = "SELECT send_time, channel, recipient, success, error_msg FROM history ORDER BY send_time DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ts = rs.getString("send_time");
                LocalDateTime time = LocalDateTime.parse(ts, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String channel = rs.getString("channel");
                String recipient = rs.getString("recipient");
                boolean success = rs.getInt("success") == 1;
                String err = rs.getString("error_msg");
                list.add(new com.pushnotifier.model.HistoryEntry(time, channel, recipient, success, err));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Ferme la connexion (à appeler avant de quitter l’application). */
    public void close() {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /** Calcule le SHA-256 d’une chaîne de caractères en entrée. */
    private String sha256(String base) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            // Convertit les bytes en hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
