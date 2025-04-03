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
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personalexpensemanager.db.TransactionRepository;
import com.example.personalexpensemanager.transaction.Transaction;
import com.example.personalexpensemanager.utility.CategorySpinnerAdapter;
import com.example.personalexpensemanager.utility.InputHintRemover;
import com.example.personalexpensemanager.utility.TypeSpinnerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionViewDetailActivity extends AppCompatActivity {

    EditText editTextAmount, editTextDescription, editTextName,editTextDate;
    Spinner spinnerCategory, spinnerType;

    Button btnSave, btnDelete;

    ImageButton btnGoback;

    TransactionRepository transactionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_view_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextName = findViewById(R.id.edit_text_name);
        spinnerCategory = findViewById(R.id.spinner_catergory);
        spinnerType = findViewById(R.id.spinner_type);
        editTextAmount = findViewById(R.id.edit_text_amount);
        editTextDate = findViewById(R.id.edit_text_date);
        editTextDescription = findViewById(R.id.edit_text_description);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnGoback = findViewById(R.id.imageButton_go_back);

        transactionRepository = new TransactionRepository(this);

        //set input fields' Hint behavior, when user click hint gone
        InputHintRemover.setHintBehavior(editTextName, "Transaction Name");
        InputHintRemover.setHintBehavior(editTextAmount, "Amount");
        InputHintRemover.setHintBehavior(editTextDescription, "Description...");

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
                    editTextAmount.setTextColor(ContextCompat.getColor(TransactionViewDetailActivity.this, R.color.colorBrightGreen));
                } else if (selectedType.equalsIgnoreCase("Expense")) {
                    editTextAmount.setTextColor(ContextCompat.getColor(TransactionViewDetailActivity.this, R.color.colorRed));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: set default
            }
        });

        //set up click to pick a date
        editTextDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    TransactionViewDetailActivity.this,
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

        //load transaction detail
        String transactionId = getIntent().getStringExtra("transactionId");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (transactionId != null  && user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .collection("transactions")
                    .document(transactionId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Transaction transaction = documentSnapshot.toObject(Transaction.class);
                            if (transaction != null) {
                                transaction.setTid(documentSnapshot.getId());
                                populateUI(transaction);
                            }
                        }
                    });
        }

        //set click button save changes event
        btnSave.setOnClickListener(v -> {
            String updatedName = editTextName.getText().toString().trim();
            String updatedCategory = spinnerCategory.getSelectedItem().toString();
            String updatedType = spinnerType.getSelectedItem().toString().toLowerCase();
            String updatedAmountText = editTextAmount.getText().toString().trim();
            String updatedDate = editTextDate.getText().toString().trim();
            String updatedDescription = editTextDescription.getText().toString().trim();

            double updatedAmount = 0;
            try {
                updatedAmount = Double.parseDouble(updatedAmountText);
            } catch (NumberFormatException e) {
                editTextAmount.setError("Invalid amount");
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                java.util.Date parsedDate = sdf.parse(updatedDate);
                com.google.firebase.Timestamp timestamp = new com.google.firebase.Timestamp(parsedDate);

                if (user != null && transactionId != null) {
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .collection("transactions")
                            .document(transactionId)
                            .update(
                                    "name", updatedName,
                                    "category", updatedCategory,
                                    "transactionType", updatedType,
                                    "amount", updatedAmount,
                                    "date", timestamp,
                                    "description", updatedDescription
                            )
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
                                finish(); // go back to previous screen
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                editTextDate.setError("Invalid date format");
                Toast.makeText(this, "Please enter a valid date (dd-MM-yyyy)", Toast.LENGTH_SHORT).show();
            }
        });

        // set delete button event and delete confirmation dialog
        btnDelete.setOnClickListener(v -> {
            if (user != null && transactionId != null) {
                new androidx.appcompat.app.AlertDialog.Builder(TransactionViewDetailActivity.this, R.style.CustomAlertDialog)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user.getUid())
                                    .collection("transactions")
                                    .document(transactionId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        //delete transaction from local room db as well
                                        transactionRepository.deleteTransactionById(transactionId);
                                        Toast.makeText(this, "Transaction deleted!", Toast.LENGTH_SHORT).show();
                                        finish(); // Return to previous screen
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Transaction not found or user not logged in", Toast.LENGTH_SHORT).show();
            }
        });


        //set click Goback button event
        btnGoback.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionViewDetailActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });


    }

    private void populateUI(Transaction transaction) {
        editTextName.setText(transaction.getName());
        editTextAmount.setText(String.format("%.2f", transaction.getAmount()));
        editTextDescription.setText(transaction.getDescription());

        if (transaction.getDate() != null) {
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(transaction.getDate().toDate());
            editTextDate.setText(formattedDate);
        }

        setSpinnerSelection(spinnerCategory, transaction.getCategory());
        setSpinnerSelection(spinnerType, transaction.getTransactionType());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }


}