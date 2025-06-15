// Fichier : HistoryPanel.java
package com.ictu.pushnotificationapp;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HistoryPanel extends JPanel {
    private final DefaultTableModel model;
    private final DashboardPanel dashboardToRefresh;

    // C'est le seul constructeur dont vous avez besoin
    public HistoryPanel(DashboardPanel dp) {
        this.dashboardToRefresh = dp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Historique des Envois"));

        model = new DefaultTableModel(new String[]{"Date/Heure", "Groupe", "Destinataire", "Message/Sujet", "Statut", "Détails"}, 0){
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Rafraîchir l'historique");
        refreshButton.addActionListener(e -> loadHistory());
        add(refreshButton, BorderLayout.SOUTH);
        
        loadHistory();
    }

    // Le constructeur vide qui causait l'erreur a été supprimé.
    
    public void loadHistory() { 
        DatabaseManager.loadHistoryIntoTable(model);
        if (dashboardToRefresh != null) {
            dashboardToRefresh.updateStats();
        }
    }
}
