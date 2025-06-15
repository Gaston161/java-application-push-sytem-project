// Fichier : EmailService.java (Corrigé)
// Utilise maintenant ConfigManager et le Validator.
// =================================================================================
package com.ictu.pushnotificationapp;


import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private final String username;
    private final String password;

    public EmailService() {
        this.username = ConfigManager.get("email.username");
        this.password = ConfigManager.get("email.password");
    }

    public boolean sendEmail(String to, String subject, String body, String attachmentPath, String groupName) {
        if (!Validator.isValidEmail(to)) {
            DatabaseManager.addLogEntry(groupName, to, subject, "Échec", "Adresse email invalide");
            return false;
        }
        
        if (username.isEmpty() || password.isEmpty() || username.contains("votre.email")) {
            String errorMsg = "Le service Email n'est pas configuré dans config.properties.";
            DatabaseManager.addLogEntry(groupName, to, subject, "Échec", errorMsg);
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
                File file = new File(attachmentPath);
                if(file.exists()){
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(file);
                    multipart.addBodyPart(attachmentPart);
                }
            }
            message.setContent(multipart);
            
            Transport.send(message);
            DatabaseManager.addLogEntry(groupName, to, subject, "Succès", "Envoyé via Gmail SMTP");
            return true;

        } catch (AuthenticationFailedException e) {
            String errorMsg = "Échec authentification Email. Vérifiez config.properties.";
            DatabaseManager.addLogEntry(groupName, to, subject, "Échec", errorMsg);
            return false;
        } catch (Exception e) {
            String errorMsg = "Erreur envoi email: " + e.getMessage();
            DatabaseManager.addLogEntry(groupName, to, subject, "Échec", errorMsg);
            return false;
        }
    }
}