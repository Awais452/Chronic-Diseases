package com.example.diseasemanagementapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Conversation {
    private String userId;
    private String userName;
    private String lastMessage;
    private long timestamp;

    // Required empty constructor for Firebase
    public Conversation() {}

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Helper method to format timestamp
    public String getFormattedTimestamp() {
        if (timestamp <= 0) return "";
        Date date = new Date(timestamp);
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }
}