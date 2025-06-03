package com.pushnotifier.model;

import java.time.LocalDateTime;

public class HistoryEntry {
    private LocalDateTime sendTime;
    private String channel;   // "EMAIL" ou "SMS"
    private String recipient; // adresse e-mail ou numéro SMS
    private boolean success;
    private String errorMsg;

    public HistoryEntry(LocalDateTime sendTime, String channel, String recipient, boolean success, String errorMsg) {
        this.sendTime = sendTime;
        this.channel = channel;
        this.recipient = recipient;
        this.success = success;
        this.errorMsg = errorMsg;
    }

    // Getters nécessaires pour PropertyValueFactory
    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public String getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
