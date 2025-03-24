package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalexpensemanager.transaction.DateHeader;
import com.example.personalexpensemanager.transaction.Transaction;
import com.example.personalexpensemanager.transaction.TransactionAdapter;
import com.example.personalexpensemanager.transaction.TransactionItem;
import com.example.personalexpensemanager.utility.DateFormatConverter;
import com.example.personalexpensemanager.utility.InputHintRemover;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    EditText etSearch;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSearch = findViewById(R.id.et_search);
        InputHintRemover.setHintBehavior(etSearch, "Search...");
        //call helper class to set the bottom navi bar
        BottomNavHelper.setupBottomNav(this);

        //load recent transactions to recyclerView
        RecyclerView rv = findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TransactionActivity.this, LoginActivity.class); // Replace LoginActivity with your actual login class
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // optional: clear back stack
            startActivity(intent);
            return;
        }

        List<TransactionItem> items = new ArrayList<>();
//        items.add(new DateHeader("Sun 23 Mar"));
//        items.add(new Transaction("uid", "Groceries", "GRACE JOEL", "desc", "expense", 4.50, null));
//        items.add(new DateHeader("Sat 22 Mar"));
//        items.add(new Transaction("uid", "Grocery", "New World", "desc", "expense", 51.83, null));
//        items.add(new Transaction("uid", "Savings", "Rapid Save", "desc", "expense", 100.00, null));

        TransactionAdapter adapter = new TransactionAdapter(items);
        rv.setAdapter(adapter);
        //load user transaction data from firebase
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String lastDate = "";

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction tx = doc.toObject(Transaction.class);

                        String formattedDate = DateFormatConverter.formatDate(tx.getDate()); // Utility function (next)
                        if (!formattedDate.equals(lastDate)) {
                            items.add(new DateHeader(formattedDate));
                            lastDate = formattedDate;
                        }

                        items.add(tx);
                    }

                    adapter.notifyDataSetChanged();
                });

    }
}