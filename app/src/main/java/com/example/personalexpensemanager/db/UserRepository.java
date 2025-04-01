package com.example.personalexpensemanager.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.personalexpensemanager.db.UserDAO;
import com.example.personalexpensemanager.db.UserDB;
import com.example.personalexpensemanager.db.UserEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

//to bridge Firestore and Room for user sync
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDAO userDAO;
    private final FirebaseFirestore firestore;

    private final MutableLiveData<List<UserEntity>> userListLiveData = new MutableLiveData<>();

    public UserRepository(Context context) {
        UserDB db = UserDB.getInstance(context);
        userDAO = (db != null) ? db.userDAO() : null;
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Loads all users from Firestore, saves them to Room, and updates LiveData.
     */
    public void fetchUsersFromFirestore() {
        firestore.collection("users")
                .get()
                .addOnSuccessListener(this::handleFirestoreResult)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch from Firestore", e));
    }

    private void handleFirestoreResult(QuerySnapshot snapshots) {
        List<UserEntity> userList = new ArrayList<>();

        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            String uid = doc.getId();
            String username = doc.getString("username");
            String email = doc.getString("email");
            String role = doc.getString("role");
            String balance = doc.getString("balance");

            if (username != null && email != null && role != null && balance != null) {
                UserEntity user = new UserEntity();
                user.setFirebaseUid(uid);
                user.setUsername(username);
                user.setEmail(email);
                user.setRole(role);
                user.setBalance(balance);
                userList.add(user);
            }
        }

        if (userDAO != null) {
            new Thread(() -> {
                userDAO.deleteAllUsers();  // Clear old cache
                for (UserEntity user : userList) {
                    userDAO.insertUser(user); // Cache updated data
                }
                userListLiveData.postValue(userList); // Notify observers
            }).start();
        }
    }

    /**
     * Reads user list from Room cache.
     */
    public LiveData<List<UserEntity>> getCachedUsers() {
        MutableLiveData<List<UserEntity>> localLiveData = new MutableLiveData<>();
        if (userDAO != null) {
            new Thread(() -> {
                List<UserEntity> cachedUsers = userDAO.getAllUsers();
                localLiveData.postValue(cachedUsers);
            }).start();
        }
        return localLiveData;
    }

    /**
     * Gets live updated list (after syncing with Firestore).
     */
    public LiveData<List<UserEntity>> getUserListLiveData() {
        return userListLiveData;
    }

    /**
     * Optionally fetch one user by Firebase UID from Room.
     */
    public LiveData<UserEntity> getUserByUid(String uid) {
        MutableLiveData<UserEntity> userLiveData = new MutableLiveData<>();
        if (userDAO != null) {
            new Thread(() -> {
                UserEntity user = userDAO.getUserByUid(uid);
                userLiveData.postValue(user);
            }).start();
        }
        return userLiveData;
    }

    public void insertUser(UserEntity user) {
        if (userDAO != null) {
            new Thread(() -> userDAO.insertUser(user)).start();
        }
    }

}
