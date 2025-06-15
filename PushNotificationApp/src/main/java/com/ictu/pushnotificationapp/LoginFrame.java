package com.ictu.pushnotificationapp;
import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Connexion Administrateur");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField userText = new JTextField(20);
        JPasswordField passText = new JPasswordField(20);
        JButton loginButton = new JButton("Connexion");
        
        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Utilisateur:"), gbc);
        gbc.gridx = 1; p.add(userText, gbc);
        gbc.gridx = 0; gbc.gridy = 1; p.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1; p.add(passText, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; p.add(loginButton, gbc);
        add(p);
        
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> {
            int userId = DatabaseManager.validateUserAndGetId(userText.getText(), new String(passText.getPassword()));
            if (userId != -1) {
                UserSession.startSession(userId, userText.getText());
                dispose();
                new MainFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Identifiants incorrects.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}