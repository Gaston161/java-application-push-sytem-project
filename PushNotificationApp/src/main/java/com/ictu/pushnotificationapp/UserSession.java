// Rôle : Classe statique pour garder en mémoire l'utilisateur connecté.
package com.ictu.pushnotificationapp;


public class UserSession {
    private static int userId;
    private static String username;
    public static void startSession(int id, String name) { userId = id; username = name; }
    public static void endSession() { userId = 0; username = null; }
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static boolean isActive() { return userId > 0; }
}