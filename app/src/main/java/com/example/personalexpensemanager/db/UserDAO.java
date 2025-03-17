package com.example.personalexpensemanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// * Data Access Object (DAO) for the User table.
// * Defines SQL operations to interact with the database.

@Dao
public interface UserDAO { // DAO -- Data Access Object

//In Room Database, DAO methods also do not need a body
// because Room automatically generates the method implementation at compile time.
    @Insert
    public void insertUser(User user);

    @Query("SELECT * FROM User")
    public List<User> getAllUsers();

//In Room’s SQL syntax, :parameter_name is a placeholder (binding variable).
//It allows dynamic query execution using method parameters.
//:username is replaced with the actual method argument when the query runs.
    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    public  User getUserByUsername(String username);

    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    public User getUserByEmail(String email);

    @Update
    public void updateUser(User user);

    @Delete
    public void deleteUser(User user);

    @Query("DELETE FROM User")
    public void deleteALLUsers();


}
