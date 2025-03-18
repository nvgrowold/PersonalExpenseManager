package com.example.personalexpensemanager.db;

import static com.example.personalexpensemanager.db.utility.PasswordHash.hashPassword;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.personalexpensemanager.db.utility.Role;

import org.jetbrains.annotations.NotNull;

//User table of RoomDB
//@Entity(indices = {@Index(value = {"username"}, unique = true),
//                   @Index(value = {"email"}, unique = true)})

/**
 * Represents a User in the system. Designed for Firestore storage.
 */
public class User {

//    @PrimaryKey(autoGenerate = true)
    private String firebaseUid; // Use Firebase UID instead of auto-generated Room ID
    @NotNull
    private String username; //required, unique
    @NotNull
    private  String email; //required, unique
    @NotNull
    private String password; //required, store in hash
    @NotNull
    private String role; //required, three type of roles: USER, ADMIN, ACCOUNTANT
//                         Store role as String instead of Enum for Firestore compatibility
    @NotNull
    private String balance = "0.00"; //required, decimal(10,2), default(0,0)

    /**
     * Default constructor required for Firestore deserialization.
     */
    public User() {}

    public User(String firebaseUid, @NotNull String username, @NotNull String email, @NotNull String password, @NotNull String role, @NotNull String balance) {
        this.firebaseUid = firebaseUid;
        this.username = username;
        this.email = email;
        this.password = hashPassword(password); // Hash password before storing
        this.role = role;
        this.balance = balance;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = hashPassword(password);
    }

    public @NotNull String getRole() {
        return role;
    }

    public void setRole(@NotNull String role) {
        this.role = role;
    }

    public @NotNull String getBalance() {
        return balance;
    }

    public void setBalance(@NotNull String balance) {
        this.balance = balance;
    }
}
