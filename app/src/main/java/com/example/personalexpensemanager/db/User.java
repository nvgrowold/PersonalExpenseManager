package com.example.personalexpensemanager.db;

import static com.example.personalexpensemanager.db.utility.PasswordHash.hashPassword;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.personalexpensemanager.db.utility.Role;

import org.jetbrains.annotations.NotNull;

//User table of RoomDB
@Entity(indices = {@Index(value = {"username"}, unique = true),
                   @Index(value = {"email"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int uid;
    @NotNull
    private String username; //required, unique
    @NotNull
    private  String email; //required, unique
    @NotNull
    private String password; //required, store in hash
    @NotNull
    private Role role; //required, three type of roles: USER, ADMIN, ACCOUNTANT
    @NotNull
    private String balance = "0.00"; //required, decimal(10,2), default(0,0)

    public User() {}

    public User(@NotNull String username, @NotNull String email, @NotNull String password, @NotNull Role role, @NotNull String balance) {
        this.username = username;
        this.email = email;
        this.password = hashPassword(password);
        this.role = role;
        this.balance = balance;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
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
        this.password = password;
    }

    public @NotNull Role getRole() {
        return role;
    }

    public void setRole(@NotNull Role role) {
        this.role = role;
    }

    public @NotNull String getBalance() {
        return balance;
    }

    public void setBalance(@NotNull String balance) {
        this.balance = balance;
    }
}
