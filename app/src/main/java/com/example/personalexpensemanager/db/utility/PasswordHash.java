package com.example.personalexpensemanager.db.utility;

import org.mindrot.jbcrypt.BCrypt;


// ============================
// PASSWORD SECURITY (BCrypt)
// ============================
public class PasswordHash { //add dependency: implementation ("org.mindrot:jbcrypt:0.4")

    public static String hashPassword(String password){ //hash plaintext password using BCrypt
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    //verify if a raw password matches stored hashed password, true if match, else false
    public static boolean checkPassword(String password, String hashedPassword){
        return BCrypt.checkpw(password,hashedPassword);
    }
}
