// =================================================================================
// Fichier : MainFrame.java
package com.ictu.pushnotificationapp;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    public MainFrame() {
        setTitle("Group 20 java  - Tableau de bord - " + UserSession.getUsername());
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DashboardPanel dashboardPanel = new DashboardPanel();
        ContactsPanel contactsPanel = new ContactsPanel();
      HistoryPanel historyPanel = new HistoryPanel(dashboardPanel);
        SendSmsPanel sendSmsPanel = new SendSmsPanel(contactsPanel, dashboardPanel, historyPanel);
        SendEmailPanel sendEmailPanel = new SendEmailPanel(contactsPanel, dashboardPanel, historyPanel);
        SchedulerPanel schedulerPanel = new SchedulerPanel(contactsPanel);
        
        JPanel navPanel = createNavPanel();
        
        mainPanel.add(dashboardPanel, "Tableau de Bord");
        mainPanel.add(sendSmsPanel, "Envoyer SMS");
        mainPanel.add(sendEmailPanel, "Envoyer Email");
        mainPanel.add(contactsPanel, "Gérer les Contacts");
        mainPanel.add(historyPanel, "Historique");
        mainPanel.add(schedulerPanel, "Planificateur");

        getContentPane().add(navPanel, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        dashboardPanel.startAutoRefresh();
    }
    
    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(new Color(45, 52, 54));
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        
        String[] navItems = {"Tableau de Bord", "Envoyer SMS", "Envoyer Email", "Gérer les Contacts", "Historique", "Planificateur"};
        for (String item : navItems) {
            JButton button = new JButton(item);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(99, 110, 114));
            button.setMargin(new Insets(10, 10, 10, 10));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            button.addActionListener(e -> cardLayout.show(mainPanel, item));
            navPanel.add(button);
            navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        navPanel.add(Box.createVerticalGlue());
        
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(214, 48, 49));
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        logoutButton.addActionListener(e -> {
            UserSession.endSession();
            dispose();
            new LoginFrame().setVisible(true);
        });
        navPanel.add(logoutButton);
        navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        return navPanel;
    }
}