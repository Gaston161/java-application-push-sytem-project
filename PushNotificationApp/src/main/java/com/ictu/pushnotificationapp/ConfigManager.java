// Rôle : Gère le chargement du fichier config.properties.
// =================================================================================
package com.ictu.pushnotificationapp;


import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    static {
        try (FileReader reader = new FileReader("config.properties")) {
            properties.load(reader);
            LOGGER.info("Fichier de configuration chargé avec succès.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "ERREUR CRITIQUE: Le fichier 'config.properties' est introuvable ou illisible.", e);
            JOptionPane.showMessageDialog(null, "Le fichier 'config.properties' est manquant ou inaccessible.\nL'application ne peut pas démarrer.", "Erreur Critique", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            LOGGER.severe("Clé de configuration '" + key + "' est manquante ou vide dans config.properties.");
            return "";
        }
        return value.trim();
    }
}
