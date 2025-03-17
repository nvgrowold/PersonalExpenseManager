package com.example.personalexpensemanager.db.utility;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.personalexpensemanager.db.User;
import com.example.personalexpensemanager.db.UserDAO;

// * Defines the Room database for User entities.
// * Uses the Singleton pattern to ensure only one instance exists.

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserDB extends RoomDatabase { //defines UserDB as a subclass of RoomDatabase
                                                    //abstract because Room generates the implementation at runtime.

    private static UserDB userDB = null; //create only one (singleton) instance of UserDB

    public abstract UserDAO userDAO(); //declare an abstract method to get an instance of UserDAO
                                        // Room will generate the implementation of UserDAO at runtime

    //build a userDB
    public static UserDB getInstance(Context context){
        if(userDB == null){  // First check (without synchronization)
            synchronized (UserDB.class) { // Use synchronized to prevent multiple threads from creating separate instances.
                if (userDB == null) {  // Second check (with synchronization)
                    userDB = Room.databaseBuilder(
                                    context.getApplicationContext(), //use the application context to prevent memory leaks
                                    UserDB.class, //specify the database class
                                    "UserDB") //set the database name
                            .fallbackToDestructiveMigration() // Deletes and recreates database if schema changes
                            .build(); //build the database instance
                }
            }
        }
        return userDB;  //return the single database instance to the caller
                        //always access UserDB using this UserDB.getInstance() method, not UserDB.userDB
    }
}
