package com.pushnotifier.service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Implémentation de SmsProvider pour l’API Orange Messagerie Pro Cameroon.
 */
public class OrangeSmsService implements SmsProvider {

    private final String endpointUrl;
    private final String authHeader; // Basic {base64(username:password)}

    /**
     * @param endpointUrl URL complète fournie par Orange, p.ex:
     *   https://api.orange.com/smsmessaging/v1/outbound/tel%3A%2B2370123456789/requests
     * @param username    identifiant API
     * @param password    mot de passe ou token
     */
    public OrangeSmsService(String endpointUrl, String username, String password) {
        this.endpointUrl = endpointUrl;
        String plainAuth = username + ":" + password;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(plainAuth.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendSms(String phoneNumber, String message) throws Exception {
        // 1) Construire la requête POST
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(endpointUrl);

            // 2) Header Authorization Basic
            post.setHeader("Authorization", authHeader);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // 3) Paramètres POST (selon doc Orange) :
            //    - 'outboundSMSMessageRequest' en JSON, contenant:
            //       > address : destinataire (ex: "tel:+2376xxxxxxx")
            //       > senderAddress : expéditeur (optionnel ou défini dans votre compte)
            //       > outboundSMSTextMessage : { "message" : "texte ici" }
            //    Pour simplifier, Orange Pro permet parfois un POST plus simple :
            //    - 'recipient' = numéro
            //    - 'message'   = texte
            //    - 'sender'    = nom expéditeur (facultatif)
            //    Vérifiez la doc précise que Orange vous a fournie.

            // Exemple générique (si Orange accepte ‘recipient’ + ‘message’):
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("recipient", phoneNumber));
            params.add(new BasicNameValuePair("message", message));
            // params.add(new BasicNameValuePair("sender", "MonApplication"));

            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            // 4) Exécuter la requête
            try (var response = client.execute(post)) {
                int statusCode = response.getCode();
                String respBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode >= 200 && statusCode < 300) {
                    // Succès Orange renvoie souvent un JSON contenant un “messageId”
                    // Par exemple : {"outboundSMSMessageRequest":{"clientCorrelator":"...","deliveryStatus":"MessageAccepted"}}
                    // On ne l’analyse pas ici, mais on pourrait vérifier “deliveryStatus”
                } else {
                    throw new RuntimeException("Orange API Error " + statusCode + " : " + respBody);
                }
            }
        }
    }
}
