package com.example.personalexpensemanager.transaction;

//for RecyclerView items in user's Dashboard
public interface TransactionItem {
    int TYPE_HEADER = 0;
    int TYPE_TRANSACTION = 1;

    int getType(); // 👈 Used to tell adapter which layout to inflate
}
