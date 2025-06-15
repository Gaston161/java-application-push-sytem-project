package com.ictu.pushnotificationapp;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities; // <--- L'IMPORT MANQUANT EST AJOUTÉ ICI
import javax.swing.SwingWorker;
import org.json.JSONObject;



public class NotificationWorker extends SwingWorker<Void, Void> {
    private static final Logger LOGGER = Logger.getLogger(NotificationWorker.class.getName());
    private final String type, groupName, subject, message, attachmentPath;
    private final List<JSONObject> recipients;
    private final Runnable onDoneCallback;

    public NotificationWorker(String type, List<JSONObject> recipients, String groupName, String subject, String message, String attachmentPath, Runnable onDoneCallback) {
        this.type = type;
        this.recipients = recipients;
        this.groupName = groupName;
        this.subject = subject;
        this.message = message;
        this.attachmentPath = attachmentPath;
        this.onDoneCallback = onDoneCallback;
    }

    @Override
    protected Void doInBackground() {
        OrangeSmsService smsService = new OrangeSmsService();
        EmailService emailService = new EmailService();

        for (JSONObject recipient : recipients) {
            if (isCancelled()) break;
            try {
                if ("SMS".equals(type)) {
                    String phone = recipient.optString("phone", null);
                    if (phone != null) {
                        smsService.sendSms(phone, message, groupName);
                    }
                } else if ("Email".equals(type)) {
                    String email = recipient.optString("email", null);
                    if (email != null) {
                        emailService.sendEmail(email, subject, message, attachmentPath, groupName);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur Worker: envoi à " + recipient, e);
            }
        }
        return null;
    }

    @Override
    protected void done() {
        if (onDoneCallback != null) {
            SwingUtilities.invokeLater(onDoneCallback);
        }
    }
}

