package com.ictu.pushnotificationapp;

/**
 *
 * @author gaston
 */
// Fichier : MainApp.java
// Rôle : Point d'entrée, lance le service de planification et la fenêtre de connexion.

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SchedulerService.getInstance().startService();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SchedulerService.getInstance().stopService()));
    }
}
