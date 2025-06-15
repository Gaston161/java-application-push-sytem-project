// =================================================================================
// Fichier : CsvUserManager.java
// Rôle : Lit et parse un fichier CSV contenant les utilisateurs.
package com.ictu.pushnotificationapp;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class CsvUserManager {
    
    // On s'attend à un CSV avec les colonnes : Nom, Telephone, Email
    public static void loadUsersIntoTable(File file, DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Vide la table avant de la remplir
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] nextLine;
            reader.readNext(); // Ignore la ligne d'en-tête (header)
            while ((nextLine = reader.readNext()) != null) {
                // Ajoute une nouvelle ligne au modèle de la table
                tableModel.addRow(new Object[]{nextLine[0], nextLine[1], nextLine[2]});
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
