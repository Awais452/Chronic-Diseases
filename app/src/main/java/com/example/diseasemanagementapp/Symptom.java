package com.example.diseasemanagementapp;

public class Symptom {
    private String name;
    private int intensity;
    private long timestamp;
    private String notes;

    public Symptom() {
        // Default constructor required for calls to DataSnapshot.getValue(Symptom.class)
    }

    public Symptom(String name, int intensity, long timestamp, String notes) {
        this.name = name;
        this.intensity = intensity;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public String getName() { return name; }
    public int getIntensity() { return intensity; }
    public long getTimestamp() { return timestamp; }
    public String getNotes() { return notes; }
}