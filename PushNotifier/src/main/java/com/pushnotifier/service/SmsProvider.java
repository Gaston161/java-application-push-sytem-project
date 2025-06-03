package com.pushnotifier.service;

/**
 * Interface générique pour envoyer un SMS.
 */
public interface SmsProvider {
    /**
     * Envoie un SMS via le provider choisi.
     * @param phoneNumber numéro destinataire (format international, ex :+2376XXXXXXXX)
     * @param message     texte du SMS (message court)
     * @throws Exception si échec
     */
    void sendSms(String phoneNumber, String message) throws Exception;
}
