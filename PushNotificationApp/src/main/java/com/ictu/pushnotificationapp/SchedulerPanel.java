package com.ictu.pushnotificationapp;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;



public class SchedulerPanel extends JPanel {
    private final DefaultTableModel model;
    private final ContactsPanel contactsPanel;

    public SchedulerPanel(ContactsPanel cp) {
        this.contactsPanel = cp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Planificateur d'envois"));

        model = new DefaultTableModel(new String[]{"ID", "Date Programmée", "Type", "Destinataires", "Statut"}, 0){
             @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton scheduleBtn = new JButton("Programmer un nouvel envoi");
        JButton cancelBtn = new JButton("Annuler la sélection");
        JButton refreshBtn = new JButton("Rafraîchir");
        bottomPanel.add(scheduleBtn);
        bottomPanel.add(cancelBtn);
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        
        refreshBtn.addActionListener(e -> loadScheduledTasks());
        
        cancelBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int scheduleId = (int) model.getValueAt(table.convertRowIndexToModel(selectedRow), 0);
                if (JOptionPane.showConfirmDialog(this, "Annuler cet envoi programmé ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    DatabaseManager.updateScheduledSendStatus(scheduleId, "Annulé");
                    loadScheduledTasks();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une tâche à annuler.", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        scheduleBtn.addActionListener(e -> showScheduleDialog());
        loadScheduledTasks();
    }

    private void loadScheduledTasks() {
        DatabaseManager.loadScheduledIntoTable(model);
    }
    
    private void showScheduleDialog() {
        JPanel dialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<String> groupList = DatabaseManager.getGroups();
        List<String> recipientChoices = new ArrayList<>();
        recipientChoices.add("Utiliser les contacts cochés");
        recipientChoices.addAll(groupList);
        JComboBox<String> recipientCombo = new JComboBox<>(recipientChoices.toArray(new String[0]));
        
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"SMS", "Email"});
        JTextField subjectField = new JTextField();
        JTextArea messageArea = new JTextArea(5, 30);
        
        JButton attachBtn = new JButton("Joindre...");
        JLabel attachmentLabel = new JLabel("Aucune pièce jointe");
        final String[] attachmentPath = {""};

        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "yyyy-MM-dd HH:mm:ss"));
        
        gbc.gridx = 0; gbc.gridy = 0; dialogPanel.add(new JLabel("Destinataires:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialogPanel.add(recipientCombo, gbc);
        
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; dialogPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialogPanel.add(typeCombo, gbc);

        JLabel subjectLabel = new JLabel("Sujet:");
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; dialogPanel.add(subjectLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialogPanel.add(subjectField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; dialogPanel.add(new JLabel("Message:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.ipady = 40; dialogPanel.add(new JScrollPane(messageArea), gbc);
        gbc.ipady = 0;
        
        JLabel attachmentTitleLabel = new JLabel("Pièce jointe:");
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; dialogPanel.add(attachmentTitleLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; dialogPanel.add(attachBtn, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; dialogPanel.add(attachmentLabel, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; dialogPanel.add(new JLabel("Date d'envoi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialogPanel.add(timeSpinner, gbc);
        
        typeCombo.addActionListener(e -> {
            boolean isEmail = "Email".equals(typeCombo.getSelectedItem());
            subjectLabel.setVisible(isEmail); subjectField.setVisible(isEmail);
            attachmentTitleLabel.setVisible(isEmail); attachBtn.setVisible(isEmail); attachmentLabel.setVisible(isEmail);
        });
        typeCombo.setSelectedIndex(1); typeCombo.setSelectedIndex(0);

        attachBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                attachmentPath[0] = file.getAbsolutePath();
                attachmentLabel.setText(file.getName());
            }
        });

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Programmer un envoi", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String message = messageArea.getText();
            if(message.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Le message ne peut être vide.", "Erreur", JOptionPane.ERROR_MESSAGE); return; }
            
            Date date = (Date) timeSpinner.getValue();
            LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (ldt.isBefore(LocalDateTime.now())) { JOptionPane.showMessageDialog(this, "La date de programmation ne peut être dans le passé.", "Erreur", JOptionPane.ERROR_MESSAGE); return; }

            String recipientChoice = (String) recipientCombo.getSelectedItem();
            String recipientType;
            String recipientName;

            if (recipientChoice.equals("Utiliser les contacts cochés")) {
                recipientType = "Individual";
                int count = contactsPanel.getSelectedContactsAsJson().size();
                if (count == 0) { JOptionPane.showMessageDialog(this, "Aucun contact n'est coché.", "Erreur", JOptionPane.ERROR_MESSAGE); return; }
                recipientName = count + " contact(s) individuel(s)";
            } else {
                recipientType = "Group";
                recipientName = recipientChoice;
            }
            
            String subject = subjectField.getText();
            String sendType = (String) typeCombo.getSelectedItem();
            
            DatabaseManager.scheduleSend(sendType, recipientType, recipientName, subject, message, attachmentPath[0], ldt);
            loadScheduledTasks();
            JOptionPane.showMessageDialog(this, "Envoi programmé avec succès !", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}