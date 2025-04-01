package com.example.personalexpensemanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personalexpensemanager.db.TransactionEntity;
import com.example.personalexpensemanager.db.TransactionRepository;
import com.example.personalexpensemanager.utility.InputHintRemover;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.example.personalexpensemanager.transaction.Transaction;

import com.example.personalexpensemanager.utility.CategorySpinnerAdapter;
import com.example.personalexpensemanager.utility.TypeSpinnerAdapter;

import java.util.Calendar;
import java.util.Locale;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TransactionAddNewActivity extends AppCompatActivity {

    Button btnSave;
    ImageButton btnGoback;
    Spinner spinnerCategory, spinnerType;
    EditText editTextAmount, editTextDescription, editTextName,editTextDate;
    TransactionRepository transactionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_add_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerCategory = findViewById(R.id.spinner_catergory);
        spinnerType = findViewById(R.id.spinner_type);
        editTextAmount = findViewById(R.id.edit_text_amount);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextName = findViewById(R.id.edit_text_name);
        editTextDate = findViewById(R.id.edit_text_date);
        btnSave = findViewById(R.id.btn_save);
        btnGoback = findViewById(R.id.imageButton_go_back);

        //set input fields' Hint behavior, when user click hint gone
        InputHintRemover.setHintBehavior(editTextName, "Transaction Name");
        InputHintRemover.setHintBehavior(editTextAmount, "Amount");
        InputHintRemover.setHintBehavior(editTextDescription, "Description...");

        //offline roomdb
        transactionRepository = new TransactionRepository(this);

        //set up spinner adapter
        String[] categories = getResources().getStringArray(R.array.transaction_categories);
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(this, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] types = getResources().getStringArray(R.array.transaction_types);
        TypeSpinnerAdapter typeAdapter = new TypeSpinnerAdapter(this, types);
        spinnerType.setAdapter(typeAdapter);


        //change amound field color based on transaction type
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();

                if (selectedType.equalsIgnoreCase("Income")) {
                    editTextAmount.setTextColor(ContextCompat.getColor(TransactionAddNewActivity.this, R.color.colorBrightGreen));
                } else if (selectedType.equalsIgnoreCase("Expense")) {
                    editTextAmount.setTextColor(ContextCompat.getColor(TransactionAddNewActivity.this, R.color.colorRed));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: set default
            }
        });

        btnSave.setOnClickListener(v -> {
                    String name = editTextName.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString();
                    String transactionType = spinnerType.getSelectedItem().toString();
                    String amountStr = editTextAmount.getText().toString().trim();
                    String dateStr = editTextDate.getText().toString().trim();
                    String description = editTextDescription.getText().toString().trim();

                    if (name.isEmpty() || category.isEmpty() || transactionType.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty()) {
                        Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                        if (transactionType.equalsIgnoreCase("Expense") && amount > 0) {
                            amount = -amount;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Timestamp timestamp;
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date parsedDate = sdf.parse(dateStr);
                        timestamp = new Timestamp(parsedDate);
                    } catch (ParseException e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Transaction transaction = new Transaction(
                            currentUser.getUid(),
                            category,
                            name,
                            description,
                            transactionType.toLowerCase(),
                            amount,
                            timestamp
                    );

                    //Save to Firestore or fallback to RoomDB
                    // Save to Firestore or fallback to RoomDB
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String userId = currentUser.getUid();

                    // Try saving to Firestore first
                    db.collection("users")
                            .document(userId)
                            .collection("transactions")
                            .add(transaction)
                            .addOnSuccessListener(documentReference -> {
                                documentReference.update("tid", documentReference.getId());
                                Toast.makeText(this, "Transaction saved online!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Fallback to RoomDB
                                String fakeTid = String.valueOf(System.currentTimeMillis()); // temporary ID
                                transaction.setTid(fakeTid);

                                TransactionEntity localTx = new TransactionEntity(
                                        fakeTid,
                                        transaction.getFirebaseUid(),
                                        transaction.getCategory(),
                                        transaction.getName(),
                                        transaction.getDescription(),
                                        transaction.getTransactionType(),
                                        transaction.getAmount(),
                                        transaction.getDate()
                                );

                                new Thread(() -> {
                                    transactionRepository.insertOrUpdate(localTx);

                                    runOnUiThread(() -> {
                                        Toast.makeText(TransactionAddNewActivity.this, "Saved offline (will sync when online)", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(TransactionAddNewActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                        new android.os.Handler().postDelayed(() -> finish(), 800);
                                    });
                                }).start();
                            });
        });

                    //set up click to pick a date
                    editTextDate.setOnClickListener(v -> {
                        final Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                TransactionAddNewActivity.this,
                                (view, selectedYear, selectedMonth, selectedDay) -> {
                                    // Format date as dd/mm/yyyy
                                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                                            selectedDay, selectedMonth + 1, selectedYear);
                                    editTextDate.setText(formattedDate);
                                },
                                year, month, day
                        );

                        datePickerDialog.show();
                    });

        //set click Goback button event
        btnGoback.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionAddNewActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });


    }
}