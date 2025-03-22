package com.example.personalexpensemanager.model;

import com.google.firebase.Timestamp;

public class Transaction {

    private String tid;             // Unique ID (optional - Firestore doc ID can be used)
    private String firebaseUid;    // Foreign Key - User ID
    private String category;       // e.g. "Food", "Transport"
    private String name;           // e.g. "New World", "Subway"
    private String description;    // optional notes
    private String type;           // "income" or "expense"
    private double amount;         // e.g. 25.00
    private Timestamp date;        // Date of transaction

    public Transaction() {
        // Required for Firestore deserialization
    }

    public Transaction(String firebaseUid, String category, String name, String description,
                       String type, double amount, Timestamp date) {
        this.firebaseUid = firebaseUid;
        this.category = category;
        this.name = name;
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
