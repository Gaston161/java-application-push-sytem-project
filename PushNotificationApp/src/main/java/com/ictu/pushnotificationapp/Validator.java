// Rôle : Centralise toutes les logiques de validation.
// =================================================================================
package com.ictu.pushnotificationapp;

public class Validator {

    /**
     * Valide une adresse email en utilisant une expression régulière standard.
     * @param email L'adresse email à valider.
     * @return true si l'email est valide, false sinon.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Regex standard pour la validation d'email
        return email.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    /**
     * Formate un numéro de téléphone pour l'API Orange.
     * Si le numéro ne commence pas par "+", il ajoute le préfixe du Cameroun.
     * @param phone Le numéro de téléphone brut.
     * @return Le numéro formaté, ou null si invalide.
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        String digitsOnly = phone.replaceAll("[\\s()-]+", ""); // Enlève les espaces
        if (digitsOnly.startsWith("+")) {
            // Supposons que le numéro est déjà au bon format international
            return digitsOnly;
        }
        if (digitsOnly.matches("^6[5-9][0-9]{7}$")) { // Format local 9 chiffres
            return "+237" + digitsOnly;
        }
        return null; // Format invalide
    }
}