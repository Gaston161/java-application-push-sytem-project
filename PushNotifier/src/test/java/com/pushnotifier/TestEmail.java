package com.pushnotifier;

import com.pushnotifier.service.EmailSender;

import java.util.Collections;

public class TestEmail {
    public static void main(String[] args) {
        // Remplacez par vos vraies infos
        String smtpHost = "smtp.gmail.com";
        int smtpPort = 587;
        String smtpUser = "votre.adresse@gmail.com";
        String smtpPass = "votreAppPassword";
        EmailSender sender = new EmailSender(smtpHost, smtpPort, smtpUser, smtpPass, smtpUser);
        try {
            sender.sendEmail(
                Collections.singletonList("destinataire@example.com"),
                "Test PushNotifier",
                "Ceci est un test depuis PushNotifier."
            );
            System.out.println("E-mail envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
