package com.example.personalexpensemanager.db;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDAO transactionDAO;
    private final FirebaseFirestore firestore;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TransactionRepository(Context context) {
        UserDB db = UserDB.getInstance(context);
        this.transactionDAO = db.transactionDAO();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<TransactionEntity>> getRecentTransactions(String uid) {
        return transactionDAO.getRecentTransactions(uid);
    }

    // Fetch all transactions (LiveData) for a user from Room
    public LiveData<List<TransactionEntity>> getAllTransactions(String firebaseUid) {
        return transactionDAO.getAllTransactions(firebaseUid);
    }

    // Insert or update multiple transactions
    public void insertOrReplaceTransactions(List<TransactionEntity> transactions) {
        executor.execute(() -> {
            for (TransactionEntity tx : transactions) {
                transactionDAO.insertOrUpdate(tx);
            }
        });
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

    public void insertOrUpdate(TransactionEntity tx) {
        executor.execute(() -> transactionDAO.insertOrUpdate(tx));
    }

    public void deleteAll() {
        new Thread(transactionDAO::deleteAllTransactions).start();
    }
}
