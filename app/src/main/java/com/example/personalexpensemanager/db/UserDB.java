package com.example.personalexpensemanager.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database for local caching (only for offline mode).
 * Firestore is the primary database.
 */

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class UserDB extends RoomDatabase { //defines UserDB as a subclass of RoomDatabase
                                                    //abstract because Room generates the implementation at runtime.

    // Singleton instance
    private static volatile UserDB INSTANCE; //create only one (singleton) instance of UserDB

    /**
     * Provides access to UserDAO for local user caching.
     */
    public abstract UserDAO userDAO(); //declare an abstract method to get an instance of UserDAO
                                        // Room will generate the implementation of UserDAO at runtime

    //build a userDB
    public static UserDB getInstance(Context context){
        boolean USE_ROOM_DB = false; // Toggle this to enable/disable RoomDB

        if (!USE_ROOM_DB) {
            return null; // Skip RoomDB initialization
        }

        if(INSTANCE == null){  // First check (without synchronization)
            synchronized (UserDB.class) { // Use synchronized to prevent multiple threads from creating separate instances.
                if (INSTANCE == null) {  // Second check (with synchronization)
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(), //use the application context to prevent memory leaks
                                    UserDB.class, //specify the database class
                                    "UserDB") //set the database name
                            .fallbackToDestructiveMigration() // Deletes and recreates database if schema changes
                            .build(); //build the database instance
                }
            }
        }
        return INSTANCE;  //return the single database instance to the caller
                        //always access UserDB using this UserDB.getInstance() method, not UserDB.userDB
    }
}
