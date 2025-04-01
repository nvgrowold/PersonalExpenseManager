package com.example.personalexpensemanager.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE firebaseUid = :uid ORDER BY date DESC LIMIT 12")
    LiveData<List<TransactionEntity>> getRecentTransactions(String uid);

    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    @Query("SELECT * FROM transactions WHERE firebaseUid = :firebaseUid ORDER BY date DESC")
    LiveData<List<TransactionEntity>> getAllTransactions(String firebaseUid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(TransactionEntity transaction);

}
