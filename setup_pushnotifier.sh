#!/usr/bin/env bash
# =================================================================
# Script d’automatisation : création de la structure complète 
#   du projet Maven PushNotifier (JavaFX + PostgreSQL + SMTP + SMS).
#
# Usage : 
#   chmod +x setup_pushnotifier.sh
#   ./setup_pushnotifier.sh
#
# Le script affiche une notification à la fin de chaque étape, 
#   puis attend que vous pressiez ENTRÉE pour passer à la suivante.
# =================================================================

# --- FONTE ET COULEUR (optionnel) ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

pause() {
  read -p "→ Appuyez sur ${GREEN}Entrée${NC} pour passer à l’étape suivante..." _
  echo
}

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 1/11 : Création du projet Maven de base ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# 1) Génération automatique d’un projet Maven via archetype
mvn archetype:generate \
  -DgroupId=com.pushnotifier \
  -DartifactId=PushNotifier \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
echo -e "${GREEN}Étape 1 terminée : Projet Maven « PushNotifier » créé.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 2/11 : Suppression des fichiers par défaut Maven ${NC}"
echo -e "${YELLOW}==============================================${NC}"
cd PushNotifier || { echo "Impossible de trouver le dossier PushNotifier !"; exit 1; }
# Supprimer la classe App.java par défaut et le dossier de test
rm -f src/main/java/com/pushnotifier/App.java
rm -rf src/test
echo -e "${GREEN}Étape 2 terminée : Fichiers par défaut supprimés.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 3/11 : Création de la structure de dossiers ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# Créer les packages Java
mkdir -p src/main/java/com/pushnotifier/model
mkdir -p src/main/java/com/pushnotifier/dao
mkdir -p src/main/java/com/pushnotifier/controller
mkdir -p src/main/java/com/pushnotifier/utils
# Créer les dossiers de ressources
mkdir -p src/main/resources/fxml
mkdir -p src/main/resources/styles
mkdir -p src/main/resources/icons
# Créer le dossier pour les tests unitaires futurs
mkdir -p src/test/java/com/pushnotifier
echo -e "${GREEN}Étape 3 terminée : Dossiers Java et resources créés.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 4/11 : Écriture du nouveau pom.xml ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# Écrase le pom.xml généré et remplace par celui adapté
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.pushnotifier</groupId>
  <artifactId>PushNotifier</artifactId>
  <version>1.0-SNAPSHOT</version>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <javafx.version>21.0.2</javafx.version>
  </properties>

  <dependencies>
    <!-- JavaFX -->
    <dependency>
      <groupId>org.openjfx</groupId>
      <artifactId>javafx-controls</artifactId>
      <version>${javafx.version}</version>
    </dependency>
    <dependency>
      <groupId>org.openjfx</groupId>
      <artifactId>javafx-fxml</artifactId>
      <version>${javafx.version}</version>
    </dependency>

    <!-- PostgreSQL JDBC Driver -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.5.1</version>
    </dependency>

    <!-- Jakarta Mail (pour l'envoi d'emails) -->
    <dependency>
      <groupId>com.sun.mail</groupId>
      <artifactId>jakarta.mail</artifactId>
      <version>2.1.1</version>
    </dependency>

    <!-- Jackson (pour JSON, si besoin) -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.15.2</version>
    </dependency>

    <!-- Apache HttpClient (pour appels HTTP API SMS) -->
    <dependency>
      <groupId>org.apache.httpcomponents.client5</groupId>
      <artifactId>httpclient5</artifactId>
      <version>5.2.1</version>
    </dependency>

    <!-- JUnit 5 pour tests unitaires -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!-- Compilation Java 17 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>${maven.compiler.source}</source>
          <target>${maven.compiler.target}</target>
        </configuration>
      </plugin>

      <!-- Plugin JavaFX pour mvn javafx:run -->
      <plugin>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-maven-plugin</artifactId>
        <version>0.0.8</version>
        <configuration>
          <mainClass>com.pushnotifier.App</mainClass>
        </configuration>
        <executions>
          <execution>
            <id>default-cli</id>
            <goals>
              <goal>run</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
EOF
echo -e "${GREEN}Étape 4 terminée : Nouveau pom.xml généré.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 5/11 : Création de la classe principale App.java ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# Créer src/main/java/com/pushnotifier/App.java
cat > src/main/java/com/pushnotifier/App.java << 'EOF'
package com.pushnotifier;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginScreen.fxml"));
        primaryStage.setTitle("Push Notifier - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
EOF
echo -e "${GREEN}Étape 5 terminée : App.java créé.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 6/11 : Création des classes modèles (model) ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# Admin.java
cat > src/main/java/com/pushnotifier/model/Admin.java << 'EOF'
package com.pushnotifier.model;

public class Admin {
    private int id;
    private String username;
    private String passwordHash;
    private String email;

    public Admin() { }

    public Admin(int id, String username, String passwordHash, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
EOF

# User.java
cat > src/main/java/com/pushnotifier/model/User.java << 'EOF'
package com.pushnotifier.model;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String groupe;
    private boolean actif;

    public User() { }

    public User(int id, String nom, String prenom, String email, String telephone, String groupe, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.groupe = groupe;
        this.actif = actif;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
EOF

# Message.java
cat > src/main/java/com/pushnotifier/model/Message.java << 'EOF'
package com.pushnotifier.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String sujet;
    private String contenu;
    private String type; // "SMS" ou "EMAIL"
    private LocalDateTime dateCreation;
    private String auteur;

    public Message() { }

    public Message(int id, String sujet, String contenu, String type, LocalDateTime dateCreation, String auteur) {
        this.id = id;
        this.sujet = sujet;
        this.contenu = contenu;
        this.type = type;
        this.dateCreation = dateCreation;
        this.auteur = auteur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
}
EOF

# Campaign.java
cat > src/main/java/com/pushnotifier/model/Campaign.java << 'EOF'
package com.pushnotifier.model;

import java.time.LocalDateTime;

public class Campaign {
    private int id;
    private int messageId;
    private LocalDateTime dateProgrammee;
    private String recurrence; // "NONE", "DAILY", "WEEKLY", ...
    private String statut;     // "EN_ATTENTE", "ENVOYÉE", "ÉCHOUÉE"

    public Campaign() { }

    public Campaign(int id, int messageId, LocalDateTime dateProgrammee, String recurrence, String statut) {
        this.id = id;
        this.messageId = messageId;
        this.dateProgrammee = dateProgrammee;
        this.recurrence = recurrence;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public LocalDateTime getDateProgrammee() { return dateProgrammee; }
    public void setDateProgrammee(LocalDateTime dateProgrammee) { this.dateProgrammee = dateProgrammee; }

    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
EOF

# LogEntry.java
cat > src/main/java/com/pushnotifier/model/LogEntry.java << 'EOF'
package com.pushnotifier.model;

import java.time.LocalDateTime;

public class LogEntry {
    private int id;
    private int campaignId;
    private int userId;
    private LocalDateTime dateEnvoi;
    private String resultat; // "SUCCÈS" ou "ÉCHEC"
    private String erreur;   // description de l’erreur

    public LogEntry() { }

    public LogEntry(int id, int campaignId, int userId, LocalDateTime dateEnvoi, String resultat, String erreur) {
        this.id = id;
        this.campaignId = campaignId;
        this.userId = userId;
        this.dateEnvoi = dateEnvoi;
        this.resultat = resultat;
        this.erreur = erreur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }

    public String getErreur() { return erreur; }
    public void setErreur(String erreur) { this.erreur = erreur; }
}
EOF

echo -e "${GREEN}Étape 6 terminée : Classes modèles créées.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 7/11 : Création des DAO (Data Access Objects) ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# DBConnection.java
cat > src/main/java/com/pushnotifier/utils/DBConnection.java << 'EOF'
package com.pushnotifier.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/pushnotifierdb";
    private static final String USER = "tonUserPostgres";
    private static final String PASSWORD = "tonMotDePasse";
    private static Connection connection;

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
EOF

# UserDAO.java
cat > src/main/java/com/pushnotifier/dao/UserDAO.java << 'EOF'
package com.pushnotifier.dao;

import com.pushnotifier.model.User;
import com.pushnotifier.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public void create(User u) throws SQLException {
        String sql = "INSERT INTO users (nom, prenom, email, telephone, groupe, actif) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getGroupe());
            ps.setBoolean(6, u.isActif());
            ps.executeUpdate();
        }
    }

    public List<User> readAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setGroupe(rs.getString("groupe"));
                u.setActif(rs.getBoolean("actif"));
                list.add(u);
            }
        }
        return list;
    }

    public void update(User u) throws SQLException {
        String sql = "UPDATE users SET nom=?, prenom=?, email=?, telephone=?, groupe=?, actif=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getGroupe());
            ps.setBoolean(6, u.isActif());
            ps.setInt(7, u.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
EOF

# MessageDAO.java
cat > src/main/java/com/pushnotifier/dao/MessageDAO.java << 'EOF'
package com.pushnotifier.dao;

import com.pushnotifier.model.Message;
import com.pushnotifier.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    public void create(Message m) throws SQLException {
        String sql = "INSERT INTO messages (sujet, contenu, type, date_creation, auteur) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getSujet());
            ps.setString(2, m.getContenu());
            ps.setString(3, m.getType());
            ps.setTimestamp(4, Timestamp.valueOf(m.getDateCreation()));
            ps.setString(5, m.getAuteur());
            ps.executeUpdate();
        }
    }

    public List<Message> readAll() throws SQLException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setSujet(rs.getString("sujet"));
                m.setContenu(rs.getString("contenu"));
                m.setType(rs.getString("type"));
                m.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                m.setAuteur(rs.getString("auteur"));
                list.add(m);
            }
        }
        return list;
    }

    public void update(Message m) throws SQLException {
        String sql = "UPDATE messages SET sujet=?, contenu=?, type=?, auteur=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getSujet());
            ps.setString(2, m.getContenu());
            ps.setString(3, m.getType());
            ps.setString(4, m.getAuteur());
            ps.setInt(5, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM messages WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
EOF

# CampaignDAO.java
cat > src/main/java/com/pushnotifier/dao/CampaignDAO.java << 'EOF'
package com.pushnotifier.dao;

import com.pushnotifier.model.Campaign;
import com.pushnotifier.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignDAO {
    public void create(Campaign c) throws SQLException {
        String sql = "INSERT INTO campaigns (message_id, date_programmee, recurrence, statut) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getMessageId());
            ps.setTimestamp(2, Timestamp.valueOf(c.getDateProgrammee()));
            ps.setString(3, c.getRecurrence());
            ps.setString(4, c.getStatut());
            ps.executeUpdate();
        }
    }

    public List<Campaign> readAll() throws SQLException {
        List<Campaign> list = new ArrayList<>();
        String sql = "SELECT * FROM campaigns ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Campaign c = new Campaign();
                c.setId(rs.getInt("id"));
                c.setMessageId(rs.getInt("message_id"));
                c.setDateProgrammee(rs.getTimestamp("date_programmee").toLocalDateTime());
                c.setRecurrence(rs.getString("recurrence"));
                c.setStatut(rs.getString("statut"));
                list.add(c);
            }
        }
        return list;
    }

    public void update(Campaign c) throws SQLException {
        String sql = "UPDATE campaigns SET date_programmee=?, recurrence=?, statut=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(c.getDateProgrammee()));
            ps.setString(2, c.getRecurrence());
            ps.setString(3, c.getStatut());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM campaigns WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
EOF

# LogDAO.java
cat > src/main/java/com/pushnotifier/dao/LogDAO.java << 'EOF'
package com.pushnotifier.dao;

import com.pushnotifier.model.LogEntry;
import com.pushnotifier.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {
    public void create(LogEntry l) throws SQLException {
        String sql = "INSERT INTO logs (campaign_id, user_id, date_envoi, resultat, erreur) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, l.getCampaignId());
            ps.setInt(2, l.getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(l.getDateEnvoi()));
            ps.setString(4, l.getResultat());
            ps.setString(5, l.getErreur());
            ps.executeUpdate();
        }
    }

    public List<LogEntry> readAll() throws SQLException {
        List<LogEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM logs ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LogEntry l = new LogEntry();
                l.setId(rs.getInt("id"));
                l.setCampaignId(rs.getInt("campaign_id"));
                l.setUserId(rs.getInt("user_id"));
                l.setDateEnvoi(rs.getTimestamp("date_envoi").toLocalDateTime());
                l.setResultat(rs.getString("resultat"));
                l.setErreur(rs.getString("erreur"));
                list.add(l);
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM logs WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
EOF

# AdminDAO.java
cat > src/main/java/com/pushnotifier/dao/AdminDAO.java << 'EOF'
package com.pushnotifier.dao;

import com.pushnotifier.model.Admin;
import com.pushnotifier.utils.DBConnection;

import java.sql.*;

public class AdminDAO {
    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin a = new Admin();
                    a.setId(rs.getInt("id"));
                    a.setUsername(rs.getString("username"));
                    a.setPasswordHash(rs.getString("password_hash"));
                    a.setEmail(rs.getString("email"));
                    return a;
                }
            }
        }
        return null;
    }
}
EOF

echo -e "${GREEN}Étape 7 terminée : DAO créés.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 8/11 : Création des utilitaires (utils) ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# EmailSender.java
cat > src/main/java/com/pushnotifier/utils/EmailSender.java << 'EOF'
package com.pushnotifier.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.List;
import java.util.Properties;
import java.io.File;

public class EmailSender {

    private final String smtpHost = "smtp.exemple.com";
    private final int smtpPort = 587;
    private final String username = "tonEmail@example.com";
    private final String password = "tonMotDePasse";

    public void sendEmail(List<String> toList, String subject, String body, List<File> attachments) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        InternetAddress[] recipients = toList.stream()
                .map(r -> {
                    try { return new InternetAddress(r); }
                    catch (Exception ex) { return null; }
                }).toArray(InternetAddress[]::new);
        msg.setRecipients(Message.RecipientType.TO, recipients);
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart();
        // Corps du message
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body, "UTF-8", "html");
        multipart.addBodyPart(textPart);

        // Pièces jointes (optionnelles)
        if (attachments != null) {
            for (File file : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(file);
                multipart.addBodyPart(attachPart);
            }
        }

        msg.setContent(multipart);
        Transport.send(msg);
    }
}
EOF

# SMSSender.java
cat > src/main/java/com/pushnotifier/utils/SMSSender.java << 'EOF'
package com.pushnotifier.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSSender {

    // Exemple pour Twilio (à adapter) :
    private final String twilioAccountSid = "TAU_ACCOUNT_ID";
    private final String twilioAuthToken = "TWILIO_AUTH_TOKEN";
    private final String twilioFromNumber = "+1234567890";

    public void sendSMS(List<String> toList, String messageText) throws Exception {
        for (String to : toList) {
            Map<String, String> data = new HashMap<>();
            data.put("To", to);
            data.put("From", twilioFromNumber);
            data.put("Body", messageText);

            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";
            String auth = java.util.Base64.getEncoder()
                            .encodeToString((twilioAccountSid + ":" + twilioAuthToken).getBytes());

            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Basic " + auth);
            post.setHeader("Content-Type", "application/json");

            String json = new ObjectMapper().writeValueAsString(data);
            post.setEntity(new StringEntity(json));

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                client.execute(post).close();
            }
        }
    }
}
EOF

# Logger.java
cat > src/main/java/com/pushnotifier/utils/Logger.java << 'EOF'
package com.pushnotifier.utils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {
    private static final String LOG_FILE = "application.log";

    public static void log(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().toString();
            out.println("[" + timestamp + "] " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

# Scheduler.java
cat > src/main/java/com/pushnotifier/utils/Scheduler.java << 'EOF'
package com.pushnotifier.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Classe utilitaire pour exécuter des tâches récurrentes 
 * (vérification des campagnes planifiées).
 */
public class Scheduler {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void start(Runnable task, long initialDelay, long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static void stop() {
        executor.shutdown();
    }
}
EOF

echo -e "${GREEN}Étape 8 terminée : Utils créés (EmailSender, SMSSender, Logger, Scheduler).${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 9/11 : Création des contrôleurs (controller) ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# LoginController.java
cat > src/main/java/com/pushnotifier/controller/LoginController.java << 'EOF'
package com.pushnotifier.controller;

import com.pushnotifier.dao.AdminDAO;
import com.pushnotifier.model.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AdminDAO adminDAO = new AdminDAO();

    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            Admin admin = adminDAO.findByUsername(username);
            if (admin != null && admin.getPasswordHash().equals(password)) {
                // Charger Dashboard.fxml
                Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Push Notifier - Tableau de bord");
                stage.setScene(new Scene(dashboardRoot));
                stage.setResizable(true);
            } else {
                errorLabel.setText("Identifiants incorrects");
            }
        } catch (Exception ex) {
            errorLabel.setText("Erreur interne : " + ex.getMessage());
        }
    }
}
EOF

# DashboardController.java
cat > src/main/java/com/pushnotifier/controller/DashboardController.java << 'EOF'
package com.pushnotifier.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Bienvenue sur le Tableau de bord");
        // TODO : initialiser les compteurs, graphiques, etc.
    }
}
EOF

# UserController.java
cat > src/main/java/com/pushnotifier/controller/UserController.java << 'EOF'
package com.pushnotifier.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;

/*
  TODO : Remplir ce contrôleur pour gérer le CRUD des utilisateurs.
  Exemple :
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    ... etc.
*/
public class UserController {
    @FXML private TableView<?> userTable;
    @FXML private TableColumn<?, ?> idColumn;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;

    @FXML
    public void initialize() {
        // TODO : Charger la liste des destinataires via UserDAO
    }
}
EOF

# MessageController.java
cat > src/main/java/com/pushnotifier/controller/MessageController.java << 'EOF'
package com.pushnotifier.controller;

/*
  TODO : Remplir ce contrôleur pour gérer la création/édition des messages.
  Exemple :
    @FXML private TextField subjectField;
    @FXML private TextArea contentArea;
    @FXML private Button saveMessageButton;
*/
public class MessageController {
    // TODO
}
EOF

# CampaignController.java
cat > src/main/java/com/pushnotifier/controller/CampaignController.java << 'EOF'
package com.pushnotifier.controller;

/*
  TODO : Remplir ce contrôleur pour gérer la planification des campagnes.
  Exemple :
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> recurrenceComboBox;
    @FXML private Button scheduleButton;
*/
public class CampaignController {
    // TODO
}
EOF

# LogController.java
cat > src/main/java/com/pushnotifier/controller/LogController.java << 'EOF'
package com.pushnotifier.controller;

/*
  TODO : Remplir ce contrôleur pour afficher l’historique des envois.
  Exemple :
    @FXML private TableView<LogEntry> logTable;
*/
public class LogController {
    // TODO
}
EOF

# StatsController.java
cat > src/main/java/com/pushnotifier/controller/StatsController.java << 'EOF'
package com.pushnotifier.controller;

/*
  TODO : Remplir ce contrôleur pour afficher les statistiques.
  Exemple :
    @FXML private Label sentCountLabel;
    @FXML private Label failedCountLabel;
*/
public class StatsController {
    // TODO
}
EOF

echo -e "${GREEN}Étape 9 terminée : Controllers créés (stubs).${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 10/11 : Création des fichiers FXML (resources/fxml) ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# LoginScreen.fxml
cat > src/main/resources/fxml/LoginScreen.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.PasswordField?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.GridPane?>

<GridPane xmlns:fx="http://javafx.com/fxml/1"
          fx:controller="com.pushnotifier.controller.LoginController"
          hgap="10" vgap="10" padding="10">
    <Label text="Utilisateur :" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
    <TextField fx:id="usernameField" prefWidth="200" GridPane.rowIndex="0" GridPane.columnIndex="1"/>

    <Label text="Mot de passe :" GridPane.rowIndex="1" GridPane.columnIndex="0"/>
    <PasswordField fx:id="passwordField" prefWidth="200" GridPane.rowIndex="1" GridPane.columnIndex="1"/>

    <Button text="Connexion" onAction="#handleLogin" GridPane.rowIndex="2" GridPane.columnIndex="1"/>

    <Label fx:id="errorLabel" textFill="red" GridPane.rowIndex="3" GridPane.columnIndex="1"/>
</GridPane>
EOF

# Dashboard.fxml
cat > src/main/resources/fxml/Dashboard.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.VBox?>

<BorderPane xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.pushnotifier.controller.DashboardController">
    <top>
        <Label fx:id="welcomeLabel" text="Tableau de bord" style="-fx-font-size: 18px; -fx-padding: 10;"/>
    </top>
    <center>
        <VBox spacing="20" style="-fx-padding: 20;">
            <!-- TODO : Ajouter les boutons ou compteurs, graphiques, etc. -->
        </VBox>
    </center>
</BorderPane>
EOF

# UserPanel.fxml
cat > src/main/resources/fxml/UserPanel.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.TableColumn?>
<?import javafx.scene.control.TableView?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>

<BorderPane xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.pushnotifier.controller.UserController">
    <top>
        <HBox spacing="10" style="-fx-padding: 10;">
            <Button text="Ajouter" fx:id="addUserButton"/>
            <Button text="Modifier" fx:id="editUserButton"/>
            <Button text="Supprimer" fx:id="deleteUserButton"/>
        </HBox>
    </top>
    <center>
        <TableView fx:id="userTable" style="-fx-padding: 10;">
            <columns>
                <TableColumn fx:id="idColumn" text="ID"/>
                <!-- TODO : Ajouter les autres colonnes (Nom, Prénom, Email, Téléphone, Groupe, Actif) -->
            </columns>
        </TableView>
    </center>
</BorderPane>
EOF

# MessagePanel.fxml
cat > src/main/resources/fxml/MessagePanel.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.TextArea?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="com.pushnotifier.controller.MessageController"
      spacing="10" style="-fx-padding: 20;">
    <TextField fx:id="subjectField" promptText="Sujet"/>
    <TextArea fx:id="contentArea" promptText="Contenu du message" prefRowCount="6"/>
    <Button text="Enregistrer le message" fx:id="saveMessageButton"/>
</VBox>
EOF

# CampaignPanel.fxml
cat > src/main/resources/fxml/CampaignPanel.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.ComboBox?>
<?import javafx.scene.control.DatePicker?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="com.pushnotifier.controller.CampaignController"
      spacing="10" style="-fx-padding: 20;">
    <DatePicker fx:id="datePicker" promptText="Date & Heure d'envoi"/>
    <ComboBox fx:id="recurrenceComboBox" promptText="Récurrence"/>
    <Button text="Planifier la campagne" fx:id="scheduleButton"/>
</VBox>
EOF

# LogPanel.fxml
cat > src/main/resources/fxml/LogPanel.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.TableColumn?>
<?import javafx.scene.control.TableView?>
<?import javafx.scene.layout.BorderPane?>

<BorderPane xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.pushnotifier.controller.LogController">
    <center>
        <TableView fx:id="logTable" style="-fx-padding: 10;">
            <columns>
                <TableColumn text="ID"/>
                <TableColumn text="Campagne"/>
                <TableColumn text="Utilisateur"/>
                <TableColumn text="Date Envoi"/>
                <TableColumn text="Résultat"/>
                <TableColumn text="Erreur"/>
            </columns>
        </TableView>
    </center>
</BorderPane>
EOF

# StatsPanel.fxml
cat > src/main/resources/fxml/StatsPanel.fxml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="com.pushnotifier.controller.StatsController"
      spacing="10" style="-fx-padding: 20;">
    <Label fx:id="sentCountLabel" text="Messages envoyés : "/>
    <Label fx:id="failedCountLabel" text="Messages échoués : "/>
    <!-- TODO : ajouter graphiques, diagrammes, etc. -->
</VBox>
EOF

echo -e "${GREEN}Étape 10 terminée : Fichiers FXML générés.${NC}"
pause

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} Étape 11/11 : Création de README.md et des dossiers styles/icons ${NC}"
echo -e "${YELLOW}==============================================${NC}"
# README.md
cat > README.md << 'EOF'
# PushNotifier

**Projet JavaFX / PostgreSQL**  
Application Desktop pour l’envoi de notifications (SMS + Email)  
Fonctionnalités :
- Authentification des administrateurs
- CRUD destinataires (utilisateurs passifs)
- Création & gestion de messages (templates)
- Planification de campagnes (immédiates, différées, récurrentes)
- Envoi par lot (batch) et en parallèle (asynchrone)
- Journalisation des envois (logs) et statistiques
- Pièces jointes pour emails

## Structure du projet

- **src/main/java/com/pushnotifier/**
  - **model/** : entités métiers (`Admin.java`, `User.java`, …)
  - **dao/** : accès BDD PostgreSQL via JDBC (`UserDAO.java`, …)
  - **controller/** : classes JavaFX contrôleurs (`LoginController.java`, …)
  - **utils/** : utilitaires (`DBConnection.java`, `EmailSender.java`, …)
  - **App.java** : classe principale (JavaFX)

- **src/main/resources/**
  - **fxml/** : vues JavaFX (FXML)
  - **styles/** : feuilles de style CSS
  - **icons/** : icônes de l’interface

## Exécution

1. Créez la base PostgreSQL `pushnotifierdb` et les tables (voir script SQL dans la doc technique).  
2. Configurez l’accès JDBC (utilisateur, mot de passe) dans `DBConnection.java`.  
3. Compilez et lancez :
   ```bash
   mvn clean javafx:run

