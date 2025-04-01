package com.example.personalexpensemanager.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.personalexpensemanager.db.utility.TimestampConverter;
import com.google.firebase.Timestamp;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey
    @NotNull
    private String tid;  // Firestore doc ID

    private String firebaseUid;
    private String category;
    private String name;
    private String description;
    private String transactionType; // income or expense
    private double amount;

    @TypeConverters(TimestampConverter.class)
    private Timestamp date;

    public TransactionEntity() {}

    @Ignore // Tell Room to ignore this constructor
    public TransactionEntity(String tid, String firebaseUid, String category, String name, String description,
                             String transactionType, double amount, Timestamp date) {
        this.tid = tid;
        this.firebaseUid = firebaseUid;
        this.category = category;
        this.name = name;
        this.description = description;
        this.transactionType = transactionType;
        this.amount = amount;
        this.date = date;
    }

    // Getters & Setters

    public String getTid() { return tid; }
    public void setTid(String tid) { this.tid = tid; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}
