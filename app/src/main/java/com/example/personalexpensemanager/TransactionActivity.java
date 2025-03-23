package com.example.personalexpensemanager;

import android.os.Bundle;
import android.widget.EditText;

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
import com.example.personalexpensemanager.utility.InputHintRemover;

import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    EditText etSearch;

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

        //load recent transactions to recyclerView
        RecyclerView rv = findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<TransactionItem> items = new ArrayList<>();
        items.add(new DateHeader("Sun 23 Mar"));
        items.add(new Transaction("uid", "Groceries", "GRACE JOEL", "desc", "expense", 4.50, null));
        items.add(new DateHeader("Sat 22 Mar"));
        items.add(new Transaction("uid", "Grocery", "New World", "desc", "expense", 51.83, null));
        items.add(new Transaction("uid", "Savings", "Rapid Save", "desc", "expense", 100.00, null));

        TransactionAdapter adapter = new TransactionAdapter(items);
        rv.setAdapter(adapter);
    }
}