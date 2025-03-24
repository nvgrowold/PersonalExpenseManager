package com.example.personalexpensemanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personalexpensemanager.transaction.Transaction;
import com.example.personalexpensemanager.utility.CategorySpinnerAdapter;
import com.example.personalexpensemanager.utility.InputHintRemover;
import com.example.personalexpensemanager.utility.TypeSpinnerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionViewDetailActivity extends AppCompatActivity {

    EditText editTextAmount, editTextDescription, editTextName,editTextDate;
    Spinner spinnerCategory, spinnerType;

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

    }

    private void populateUI(Transaction transaction) {
        editTextName.setText(transaction.getName());
        editTextAmount.setText(String.format("%.2f", transaction.getAmount()));
        editTextDescription.setText(transaction.getDescription());

        if (transaction.getDate() != null) {
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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