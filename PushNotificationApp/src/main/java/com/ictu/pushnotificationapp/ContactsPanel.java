// Fichier : ContactsPanel.java (Complet)
package com.ictu.pushnotificationapp;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import org.json.JSONObject;

public class ContactsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public ContactsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Gestion des Contacts et Groupes"));

        model = new DefaultTableModel(new Object[]{"ID", "Nom", "Téléphone", "Email", "Sélection"}, 0) {
            @Override public Class<?> getColumnClass(int i) { return i == 4 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Ajouter Contact");
        JButton delBtn = new JButton("Supprimer Sélection");
        JButton importBtn = new JButton("Importer CSV");
        JButton createGroupBtn = new JButton("Créer Groupe");
        JButton addToGroupBtn = new JButton("Ajouter Sélection au Groupe");
        
        buttonPanel.add(addBtn);
        buttonPanel.add(delBtn);
        buttonPanel.add(importBtn);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(createGroupBtn);
        buttonPanel.add(addToGroupBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addContact());
        delBtn.addActionListener(e -> deleteSelectedContacts());
        importBtn.addActionListener(e -> importFromCsv());
        createGroupBtn.addActionListener(e -> createGroup());
        addToGroupBtn.addActionListener(e -> addToGroup());
        
        loadContacts();
    }
    
    public void loadContacts() { DatabaseManager.loadContactsIntoTable(model); }
    
    private void addContact() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        Object[] message = {"Nom:", nameField, "Téléphone:", phoneField, "Email:", emailField};
        int option = JOptionPane.showConfirmDialog(this, message, "Ajouter un Contact", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            DatabaseManager.addContact(nameField.getText(), phoneField.getText(), emailField.getText());
            loadContacts();
        }
    }

    private void deleteSelectedContacts() {
        if (JOptionPane.showConfirmDialog(this, "Supprimer les contacts sélectionnés ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                if ((Boolean) model.getValueAt(i, 4)) {
                    int contactId = (int) model.getValueAt(i, 0);
                    DatabaseManager.deleteContact(contactId);
                }
            }
            loadContacts();
        }
    }

    private void importFromCsv() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                reader.readNext();
                String[] line;
                int count = 0;
                while ((line = reader.readNext()) != null) {
                    DatabaseManager.addContact(line[0], line[1], line[2]);
                    count++;
                }
                loadContacts();
                JOptionPane.showMessageDialog(this, count + " contact(s) importé(s).");
            } catch (IOException | CsvValidationException ex) {
                JOptionPane.showMessageDialog(this, "Erreur de lecture du fichier CSV.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Nom du nouveau groupe:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            DatabaseManager.createGroup(groupName);
            JOptionPane.showMessageDialog(this, "Groupe '" + groupName + "' créé.");
        }
    }

    private void addToGroup() {
        List<Integer> selectedContactIds = getSelectedContactIds();
        if (selectedContactIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez cocher les contacts à ajouter au groupe.", "Aucune Sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> groups = DatabaseManager.getGroups();
        if (groups.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun groupe n'a été créé.", "Aucun Groupe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedGroup = (String) JOptionPane.showInputDialog(this, 
            "Choisir un groupe:", "Ajouter au Groupe", 
            JOptionPane.QUESTION_MESSAGE, null, 
            groups.toArray(), groups.get(0));

        if (selectedGroup != null) {
            int groupId = DatabaseManager.getGroupId(selectedGroup);
            if(groupId != -1) {
                DatabaseManager.addContactsToGroup(selectedContactIds, groupId);
                JOptionPane.showMessageDialog(this, selectedContactIds.size() + " contact(s) ajoutés au groupe '" + selectedGroup + "'.");
            }
        }
    }
    
    public List<JSONObject> getSelectedContactsAsJson() {
        List<JSONObject> selected = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 4)) {
                JSONObject contact = new JSONObject();
                contact.put("name", model.getValueAt(i, 1));
                contact.put("phone", model.getValueAt(i, 2));
                contact.put("email", model.getValueAt(i, 3));
                selected.add(contact);
            }
        }
        return selected;
    }
    
    private List<Integer> getSelectedContactIds() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 4)) {
                ids.add((Integer) model.getValueAt(i, 0));
            }
        }
        return ids;
    }
}
