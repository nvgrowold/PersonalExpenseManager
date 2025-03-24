package com.example.personalexpensemanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    Spinner spinnerCategory, spinnerType;
    EditText editTextAmount, editTextDescription, editTextName,editTextDate;

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

        // Save button event
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

            // Parse and format amount
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
            // Parse user input transaction date (dateStr) to Timestamp
            Timestamp timestamp;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date parsedDate = sdf.parse(dateStr);
                timestamp = new Timestamp(parsedDate);
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create transaction object
            Transaction transaction = new Transaction(
                    currentUser.getUid(),
                    category,
                    name,
                    description,
                    transactionType.toLowerCase(), // "income" or "expense"
                    amount,
//                    Timestamp.now() // Replace with custom parsed date if needed
                    timestamp
            );

            // Save to Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = currentUser.getUid();

            db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(documentReference -> {
                        // Set transaction ID (tid) field in the same doc
                        documentReference.update("tid", documentReference.getId());

                        Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous screen
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
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
                        // Format date as dd/MM/yyyy
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        editTextDate.setText(formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });


    }
}