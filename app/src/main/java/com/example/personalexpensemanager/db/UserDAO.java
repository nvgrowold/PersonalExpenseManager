package com.example.personalexpensemanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// * Data Access Object (DAO) for the User table.
// * Defines SQL operations to interact with the database.
// * in this project, used only for offline caching since Firestore is the main database.
@Dao
public interface UserDAO { // DAO -- Data Access Object

//In Room Database, DAO methods also do not need a body
// because Room automatically generates the method implementation at compile time.
    /**
     * Inserts or updates a user in the local cache.
     * If the user already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

//In Room’s SQL syntax, :parameter_name is a placeholder (binding variable).
//It allows dynamic query execution using method parameters.
//:username is replaced with the actual method argument when the query runs.
    /**
     * Deletes a specific user from the local cache.
     */
//    @Delete
//    void deleteUser(User user);

    /**
     * Clears all user data from the local cache (e.g., on logout).
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("SELECT * FROM users ORDER BY username ASC")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users WHERE firebaseUid = :uid")
    UserEntity getUserByUid(String uid);

}
