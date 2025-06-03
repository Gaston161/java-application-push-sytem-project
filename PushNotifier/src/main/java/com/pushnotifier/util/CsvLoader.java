package com.pushnotifier.util;

import com.pushnotifier.model.Recipient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour charger un fichier CSV de destinataires.
 * Format attendu :
 *   - Première ligne (optionnelle) d'en-tête contenant “Name,Email,Phone”
 *   - Lignes suivantes : prénom et nom, email, numéroPhone
 */
public class CsvLoader {

    public static List<Recipient> loadRecipients(String csvFilePath) {
        List<Recipient> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                // Si première ligne contient "Name" et "Email", on la saute
                if (firstLine && line.toLowerCase().contains("name") && line.toLowerCase().contains("email")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String email = parts[1].trim();
                    String phone = parts[2].trim();
                    list.add(new Recipient(name, email, phone));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
