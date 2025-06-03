package com.pushnotifier.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.List;
import java.util.Properties;

/**
 * Service pour envoyer des e-mails via SMTP (JavaMail / Jakarta Mail).
 */
public class EmailSender {
    private final Session session;
    private final String fromAddress;

    /**
     * Constructeur.
     * @param smtpHost hôte SMTP (ex : smtp.gmail.com)
     * @param smtpPort port SMTP (ex : 587)
     * @param userIdentifiant (login) pour l’authentification SMTP
     * @param userPassword mot de passe ou mot de passe d’application
     * @param fromAddress l’adresse d’envoi (From)
     */
    public EmailSender(String smtpHost, int smtpPort, String userIdentifiant, String userPassword, String fromAddress) {
        this.fromAddress = fromAddress;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userIdentifiant, userPassword);
            }
        });
    }

    /**
     * Envoie un e-mail à une liste de destinataires (en BCC pour ne pas exposer les adresses).
     * @param toList liste d’adresses e-mail
     * @param subject objet du message
     * @param body    corps du message (texte brut)
     * @throws MessagingException si échec
     */
    public void sendEmail(List<String> toList, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress));
        for (String to : toList) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(to));
        }
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }
}
