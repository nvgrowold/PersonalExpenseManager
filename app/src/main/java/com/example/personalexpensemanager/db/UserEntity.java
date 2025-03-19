package com.example.personalexpensemanager.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users") // Define table name for RoomDB
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String email;
    private String role;
    private String balance;

    // Default constructor required by Room
    public UserEntity() {}

    // ✅ Room should ignore this constructor
    @Ignore
    public UserEntity(String username, String email, String role, String balance) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBalance() { return balance; }
    public void setBalance(String balance) { this.balance = balance; }
}
