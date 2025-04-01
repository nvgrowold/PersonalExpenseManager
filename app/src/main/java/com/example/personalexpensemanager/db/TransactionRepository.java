package com.example.personalexpensemanager.db;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class TransactionRepository {

    private final TransactionDAO transactionDAO;
    private final FirebaseFirestore firestore;

    public TransactionRepository(Context context) {
        UserDB db = UserDB.getInstance(context);
        this.transactionDAO = db.transactionDAO();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<TransactionEntity>> getRecentTransactions(String uid) {
        return transactionDAO.getRecentTransactions(uid);
    }

    public void fetchAndCacheTransactions(String uid) {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(12)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (var doc : querySnapshots) {
                        try {
                            TransactionEntity tx = doc.toObject(TransactionEntity.class);
                            tx.setTid(doc.getId()); // use Firestore document ID
                            new Thread(() -> transactionDAO.insertTransaction(tx)).start();
                        } catch (Exception e) {
                            Log.e("TransactionRepo", "Mapping failed: " + e.getMessage());
                        }
                    }
                    Log.d("TransactionRepo", "Synced transactions from Firestore to Room");
                })
                .addOnFailureListener(e -> Log.e("TransactionRepo", "Failed to fetch Firestore transactions", e));
    }

    public void deleteAll() {
        new Thread(transactionDAO::deleteAllTransactions).start();
    }
}
