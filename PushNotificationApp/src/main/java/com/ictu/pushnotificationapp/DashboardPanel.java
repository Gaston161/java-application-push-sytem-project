// =================================================================================
// Fichiers des Panneaux de l'Interface Graphique
// =================================================================================

// Fichier : DashboardPanel.java
package com.ictu.pushnotificationapp;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardPanel extends JPanel {
    private final JLabel totalLabel, successLabel, failedLabel;
    private Timer refreshTimer;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Tableau de Bord");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        totalLabel = createStatCard(statsPanel, "TOTAL ENVOYÉ", "0", new Color(224, 236, 255));
        successLabel = createStatCard(statsPanel, "SUCCÈS", "0", new Color(222, 255, 227));
        failedLabel = createStatCard(statsPanel, "ÉCHECS", "0", new Color(255, 224, 224));
        add(statsPanel, BorderLayout.CENTER);
        updateStats();
    }

    private JLabel createStatCard(JPanel p, String title, String val, Color c) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, c));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(10, 15, 0, 10));
        JLabel valueLabel = new JLabel(val);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setBorder(new EmptyBorder(0, 15, 10, 10));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        p.add(card);
        return valueLabel;
    }
    
    public void updateStats() {
        int[] stats = DatabaseManager.getDashboardStats();
        totalLabel.setText(String.valueOf(stats[0]));
        successLabel.setText(String.valueOf(stats[1]));
        failedLabel.setText(String.valueOf(stats[2]));
    }

    public void startAutoRefresh() {
        if(refreshTimer == null) {
            refreshTimer = new Timer(5000, e -> updateStats());
            refreshTimer.start();
        }
    }
}
