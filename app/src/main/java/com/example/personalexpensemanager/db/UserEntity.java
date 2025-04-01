package com.example.personalexpensemanager.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

//*****************
//For Room DB
@Entity(
        tableName = "users",
        indices = {@Index(value = {"firebaseUid"}, unique = true)}) // Define table name for RoomDB
public class UserEntity {

    @PrimaryKey
    @NotNull
    private String firebaseUid; // Firebase UID as a primary key (or indexed)
    private String username;
    private String email;
    private String role;
    private String balance;



    // Default constructor required by Room
    public UserEntity() {}


    // ✅ Room should ignore this constructor
    @Ignore
    public UserEntity(@NotNull String firebaseUid, String username, String email, String role, String balance) {
        this.firebaseUid = firebaseUid;
        this.username = username;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }

    // Getters & Setters

    public @NotNull String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(@NotNull String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
