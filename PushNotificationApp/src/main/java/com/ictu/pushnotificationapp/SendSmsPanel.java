package com.ictu.pushnotificationapp;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.json.JSONObject;

public class SendSmsPanel extends JPanel {
    private final JTextArea messageArea;
    private final JLabel recipientsLabel;
    private List<JSONObject> selectedRecipients = new ArrayList<>();
    private String selectedGroupName = "Individuel";

    public SendSmsPanel(ContactsPanel cp, DashboardPanel dp, HistoryPanel hp) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Envoyer des SMS"));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel recipientButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectIndividualBtn = new JButton("Choisir des Contacts Individuels...");
        JButton selectGroupBtn = new JButton("Choisir un Groupe...");
        recipientButtons.add(selectIndividualBtn);
        recipientButtons.add(selectGroupBtn);
        
        recipientsLabel = new JLabel("Aucun destinataire sélectionné.");
        topPanel.add(recipientButtons, BorderLayout.NORTH);
        topPanel.add(recipientsLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        messageArea = new JTextArea(10, 50);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JButton sendButton = new JButton("Envoyer Maintenant");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(sendButton);
        add(bottomPanel, BorderLayout.SOUTH);

        selectIndividualBtn.addActionListener(e -> {
            selectedRecipients = cp.getSelectedContactsAsJson();
            selectedGroupName = "Individuel";
            recipientsLabel.setText(selectedRecipients.size() + " contact(s) individuel(s) sélectionné(s).");
        });

        selectGroupBtn.addActionListener(e -> {
            List<String> groups = DatabaseManager.getGroups();
            if (groups.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun groupe n'a été créé.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String group = (String) JOptionPane.showInputDialog(this, "Choisir un groupe destinataire:", "Envoi Groupé", JOptionPane.QUESTION_MESSAGE, null, groups.toArray(), groups.get(0));
            if (group != null) {
                selectedRecipients = DatabaseManager.getContactsByGroup(group);
                selectedGroupName = group;
                recipientsLabel.setText("Groupe '" + group + "' (" + selectedRecipients.size() + " membres) sélectionné.");
            }
        });

        sendButton.addActionListener(e -> {
            String message = messageArea.getText();
            if (selectedRecipients.isEmpty() || message.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez choisir des destinataires et écrire un message.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Envoyer ce SMS à " + selectedRecipients.size() + " destinataire(s) ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new NotificationWorker("SMS", selectedRecipients, selectedGroupName, null, message, null, () -> {
                    dp.updateStats();
                    hp.loadHistory();
                }).execute();
            }
        });
    }
}

