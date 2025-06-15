package com.ictu.pushnotificationapp;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.json.JSONObject;

public class SendEmailPanel extends JPanel {
    private final JTextField subjectField = new JTextField();
    private final JTextArea messageArea = new JTextArea(10, 50);
    private final JLabel attachmentLabel = new JLabel("Aucune pièce jointe");
    private final JLabel recipientsLabel = new JLabel("Aucun destinataire sélectionné.");
    private String attachmentPath = "";
    private List<JSONObject> selectedRecipients = new ArrayList<>();
    private String selectedGroupName = "Individuel";

    public SendEmailPanel(ContactsPanel cp, DashboardPanel dp, HistoryPanel hp) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Envoyer des Emails"));

        JPanel topControls = new JPanel(new BorderLayout(10,10));
        
        JPanel recipientButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectIndividualBtn = new JButton("Choisir des Contacts Individuels...");
        JButton selectGroupBtn = new JButton("Choisir un Groupe...");
        recipientButtons.add(selectIndividualBtn);
        recipientButtons.add(selectGroupBtn);
        
        JPanel recipientPanel = new JPanel(new BorderLayout());
        recipientPanel.add(recipientButtons, BorderLayout.NORTH);
        recipientPanel.add(recipientsLabel, BorderLayout.CENTER);

        topControls.add(recipientPanel, BorderLayout.NORTH);
        
        JPanel subjectPanel = new JPanel(new BorderLayout(5, 5));
        subjectPanel.add(new JLabel("Sujet:"), BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        topControls.add(subjectPanel, BorderLayout.CENTER);

        add(topControls, BorderLayout.NORTH);
        
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton attachButton = new JButton("Joindre un fichier");
        attachmentPanel.add(attachButton);
        attachmentPanel.add(attachmentLabel);
        
        JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Envoyer Maintenant");
        sendPanel.add(sendButton);

        bottomPanel.add(attachmentPanel, BorderLayout.WEST);
        bottomPanel.add(sendPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        
        selectIndividualBtn.addActionListener(e -> {
            selectedRecipients = cp.getSelectedContactsAsJson();
            selectedGroupName = "Individuel";
            recipientsLabel.setText(selectedRecipients.size() + " contact(s) individuel(s) sélectionné(s).");
        });

        selectGroupBtn.addActionListener(e -> {
            List<String> groups = DatabaseManager.getGroups();
            if (groups.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun groupe n'a été créé.", "Erreur", JOptionPane.WARNING_MESSAGE); return; }
            String group = (String) JOptionPane.showInputDialog(this, "Choisir un groupe destinataire:", "Envoi Groupé", JOptionPane.QUESTION_MESSAGE, null, groups.toArray(), groups.get(0));
            if (group != null) {
                selectedRecipients = DatabaseManager.getContactsByGroup(group);
                selectedGroupName = group;
                recipientsLabel.setText("Groupe '" + group + "' (" + selectedRecipients.size() + " membres) sélectionné.");
            }
        });

        attachButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                attachmentPath = file.getAbsolutePath();
                attachmentLabel.setText(file.getName());
            }
        });

        sendButton.addActionListener(e -> {
            String subject = subjectField.getText();
            String message = messageArea.getText();
            if (selectedRecipients.isEmpty() || message.trim().isEmpty() || subject.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez choisir des destinataires, un sujet et un message.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Envoyer cet email à " + selectedRecipients.size() + " destinataire(s) ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                 new NotificationWorker("Email", selectedRecipients, selectedGroupName, subject, message, attachmentPath, () -> {
                    dp.updateStats();
                    hp.loadHistory();
                 }).execute();
            }
        });
    }
}