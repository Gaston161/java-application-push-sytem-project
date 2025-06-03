package com.pushnotifier;

import com.pushnotifier.service.DatabaseService;

public class TestDatabase {
    public static void main(String[] args) {
        try {
            DatabaseService db = new DatabaseService();
            boolean ok = db.authenticateAdmin("admin", "admin123");
            System.out.println("Authent admin/admin123 : " + ok);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
