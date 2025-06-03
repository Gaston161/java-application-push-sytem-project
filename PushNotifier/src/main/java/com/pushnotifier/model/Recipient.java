package com.pushnotifier.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Recipient {
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty phone;
    private final SimpleBooleanProperty selected;

    public Recipient(String name, String email, String phone) {
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.selected = new SimpleBooleanProperty(false);
    }

    // Getters & Setters
    public String getName() { return name.get(); }
    public void setName(String n) { name.set(n); }

    public String getEmail() { return email.get(); }
    public void setEmail(String e) { email.set(e); }

    public String getPhone() { return phone.get(); }
    public void setPhone(String p) { phone.set(p); }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean s) { selected.set(s); }
    public SimpleBooleanProperty selectedProperty() { return selected; }
}
