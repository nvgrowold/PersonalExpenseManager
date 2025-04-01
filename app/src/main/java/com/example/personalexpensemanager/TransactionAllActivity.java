package com.example.personalexpensemanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalexpensemanager.db.TransactionEntity;
import com.example.personalexpensemanager.db.TransactionRepository;
import com.example.personalexpensemanager.transaction.DateHeader;
import com.example.personalexpensemanager.transaction.Transaction;
import com.example.personalexpensemanager.transaction.TransactionAdapter;
import com.example.personalexpensemanager.transaction.TransactionItem;
import com.example.personalexpensemanager.utility.BottomNavHelper;
import com.example.personalexpensemanager.utility.DateFormatConverter;
import com.example.personalexpensemanager.utility.InputHintRemover;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAllActivity extends AppCompatActivity {

    EditText etSearch, etStartDate, etEndDate;
    Button btnClearFilter;
    FirebaseUser user;
    Timestamp startDate = null, endDate = null;
    List<Transaction> allTransactions = new ArrayList<>();
    List<TransactionItem> displayItems = new ArrayList<>();
    TransactionAdapter adapter;
    //for RoomDB offline function
    TransactionRepository transactionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_all);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSearch = findViewById(R.id.et_search);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        InputHintRemover.setHintBehavior(etSearch, "Search...");
        btnClearFilter = findViewById(R.id.btn_clear_filters);
        //call helper class to set the bottom navi bar
        BottomNavHelper.setupBottomNav(this);

        //recyclerView setup must before any filtering
        //load recent transactions to recyclerView
        RecyclerView rv = findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TransactionAllActivity.this, LoginActivity.class); // Replace LoginActivity with your actual login class
            startActivity(intent);
            return;
        }

        //for roomdb offline function
        transactionRepository = new TransactionRepository(this);
        transactionRepository.getAllTransactions(user.getUid()).observe(this, entityList -> {
            allTransactions.clear();
            for (TransactionEntity entity : entityList) {
                Transaction tx = new Transaction();
                tx.setTid(entity.getTid());
                tx.setName(entity.getName());
                tx.setAmount(entity.getAmount());
                tx.setTransactionType(entity.getTransactionType());
                tx.setCategory(entity.getCategory());
                tx.setDate(entity.getDate());
                tx.setDescription(entity.getDescription());
                tx.setFirebaseUid(entity.getFirebaseUid());
                allTransactions.add(tx);
            }
            filterTransactions();
        });

        adapter = new TransactionAdapter(displayItems, transaction -> {
            Intent intent = new Intent(TransactionAllActivity.this, TransactionViewDetailActivity.class);
            intent.putExtra("transactionId", transaction.getTid());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions();
                updateClearFilterVisibility();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate, true));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate, false));

        btnClearFilter.setOnClickListener(v -> {
            etSearch.setText("");
            etStartDate.setText("");
            etEndDate.setText("");
            startDate = null;
            endDate = null;
            updateClearFilterVisibility();
            filterTransactions();
        });

        // Keep this to update Room cache when online
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TransactionEntity> roomData = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction tx = doc.toObject(Transaction.class);
                        tx.setTid(doc.getId());
                        TransactionEntity entity = new TransactionEntity(
                                tx.getTid(), tx.getFirebaseUid(), tx.getCategory(),
                                tx.getName(), tx.getDescription(), tx.getTransactionType(),
                                tx.getAmount(), tx.getDate()
                        );
                        roomData.add(entity);
                    }
                    transactionRepository.insertOrReplaceTransactions(roomData);
                });

    }

    private void showDatePickerDialog(EditText editText, boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int year, int month, int day) -> {
            calendar.set(year, month, day, 0, 0, 0);
            Date picked = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editText.setText(sdf.format(picked));
            if (isStart) startDate = new Timestamp(picked);
            else endDate = new Timestamp(picked);
            updateClearFilterVisibility();
            filterTransactions();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void filterTransactions() {
        displayItems.clear();
        String keyword = etSearch.getText().toString().toLowerCase(Locale.getDefault()).trim();
        String lastDateFormatted = "";

        for (Transaction tx : allTransactions) {
            boolean matchesKeyword = keyword.isEmpty()
                    || tx.getName().toLowerCase().contains(keyword)
                    || tx.getCategory().toLowerCase().contains(keyword)
                    || tx.getDescription().toLowerCase().contains(keyword);

            boolean matchesDate = true;
            if (startDate != null && tx.getDate().compareTo(startDate) < 0) matchesDate = false;
            if (endDate != null && tx.getDate().compareTo(endDate) > 0) matchesDate = false;

            if (matchesKeyword && matchesDate) {
                String formattedDate = DateFormatConverter.formatDate(tx.getDate());
                if (!formattedDate.equals(lastDateFormatted)) {
                    displayItems.add(new DateHeader(formattedDate));
                    lastDateFormatted = formattedDate;
                }
                displayItems.add(tx);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void updateClearFilterVisibility() {
        boolean hasSearch = !etSearch.getText().toString().trim().isEmpty();
        boolean hasStartDate = startDate != null;
        boolean hasEndDate = endDate != null;

        if (hasSearch || hasStartDate || hasEndDate) {
            btnClearFilter.setVisibility(View.VISIBLE);
        } else {
            btnClearFilter.setVisibility(View.GONE);
        }
    }

}