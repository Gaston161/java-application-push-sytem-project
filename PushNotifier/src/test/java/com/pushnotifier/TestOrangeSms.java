package com.pushnotifier;

import com.pushnotifier.service.OrangeSmsService;

public class TestOrangeSms {
    public static void main(String[] args) {
        // Remplacez par vos infos Orange API
        String endpoint = "https://api.orange.com/smsmessaging/v1/outbound/tel%3A%2B2370123456789/requests";
        String user = "VotreOrangeUsername";
        String pass = "VotreOrangePassword";
        OrangeSmsService sms = new OrangeSmsService(endpoint, user, pass);
        try {
            sms.sendSms("+2376XXXXXXXX", "Test depuis Orange API Messagerie Pro");
            System.out.println("SMS envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
