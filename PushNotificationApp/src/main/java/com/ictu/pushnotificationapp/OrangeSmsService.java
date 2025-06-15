// Fichier : OrangeSmsService.java (Corrigé)
// Utilise maintenant ConfigManager et le Validator.
// =================================================================================
package com.ictu.pushnotificationapp;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class OrangeSmsService {
    private static final Logger LOGGER = Logger.getLogger(OrangeSmsService.class.getName());
    private final String authorizationHeader;
    private final String senderName;
    private String accessToken;

    public OrangeSmsService() {
        this.authorizationHeader = ConfigManager.get("orange.auth.header");
        this.senderName = ConfigManager.get("orange.sender.name");
    }

    private boolean getAccessToken() {
        if (authorizationHeader.isEmpty() || authorizationHeader.equals("Basic Ym1pYjhlSG1QbXNxcnBDZnlyYnJFa05adldrUE9ucEk6bEZ6NVdXTnZFNFM0dUNCazJlb0lJZVRiMnJub0FVcXJKU0NUWEVrTkQ4NEg=")) {
             LOGGER.severe("Header d'autorisation Orange non configuré dans config.properties.");
             return false;
        }
        if (accessToken != null) return true;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.orange.com/oauth/v3/token"))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .setHeader("Authorization", authorizationHeader)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Accept", "application/json").build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                this.accessToken = new JSONObject(response.body()).getString("access_token");
                return true;
            } else {
                LOGGER.severe("Erreur d'obtention du token Orange: " + response.body());
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors de la demande de token Orange", e);
            return false;
        }
    }

    public boolean sendSms(String recipient, String message, String groupName) {
        String formattedRecipient = Validator.formatPhoneNumber(recipient);
        if (formattedRecipient == null) {
            DatabaseManager.addLogEntry(groupName, recipient, message, "Échec", "Numéro invalide");
            return false;
        }
        if (!getAccessToken()) {
            DatabaseManager.addLogEntry(groupName, formattedRecipient, message, "Échec", "Token d'accès Orange invalide");
            return false;
        }

        // NOTE: L'API d'Orange est très spécifique. Le `devApiNumber` est un placeholder.
        // Vous devez trouver l'adresse de l'expéditeur ("senderAddress") correcte pour votre
        // compte dans votre espace développeur Orange. Cela pourrait être un numéro court ou un code.
        String devApiNumberForUrl = "8180"; // A VERIFIER DANS VOTRE ESPACE ORANGE DEV
        String sendUrl = "https://api.orange.com/smsmessaging/v1/outbound/tel%3A%2B" + devApiNumberForUrl + "/requests";
        
        String jsonBody = new JSONObject().put("outboundSMSMessageRequest", new JSONObject()
                .put("address", "tel:" + formattedRecipient)
                .put("senderAddress", "tel:+" + devApiNumberForUrl) // Doit correspondre à l'URL
                .put("senderName", this.senderName) // C'est ce que l'utilisateur verra
                .put("outboundSMSTextMessage", new JSONObject().put("message", message))).toString();
        
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(sendUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .setHeader("Authorization", "Bearer " + this.accessToken)
                .setHeader("Content-Type", "application/json").build();
            
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
            boolean success = response.statusCode() == 201;
            String statusDetails = "API Response: " + response.statusCode();

            DatabaseManager.addLogEntry(groupName, formattedRecipient, message, success ? "Succès" : "Échec", statusDetails);
            return success;
        } catch (Exception e) {
            DatabaseManager.addLogEntry(groupName, formattedRecipient, message, "Échec", e.getMessage());
            return false;
        }
    }
}