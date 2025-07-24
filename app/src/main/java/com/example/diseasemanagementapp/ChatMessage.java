package com.example.diseasemanagementapp;

public class ChatMessage {
    private String text;
    private String senderId;
    private long timestamp;

    // IMPORTANT: A default, no-argument constructor is required for Firebase to deserialize messages.
    public ChatMessage() {
    }

    public ChatMessage(String text, String senderId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // --- Getters and Setters ---
    // Firebase needs these to populate the object from the database snapshot.

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}