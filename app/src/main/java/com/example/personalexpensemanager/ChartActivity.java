package com.example.personalexpensemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChartActivity extends AppCompatActivity {


    private Button btnRequestService, btnDownloadIRForm;
    private TextView tvUsername;
    private String currentUserId;
    private String latestFormId = null; // dynamically loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvUsername = findViewById(R.id.tv_username);
        btnRequestService = findViewById(R.id.btn_request_service);
        btnDownloadIRForm = findViewById(R.id.btn_download_filled_IR_form);
        btnDownloadIRForm.setVisibility(View.GONE); // default hide

        loadUserName();
        checkIfPdfExists();

        //send user accountant service request to firestore
        btnRequestService.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(currentUserId)
                    .set(new java.util.HashMap<String, Object>() {{
                        put("requestedAt", new java.util.Date());
                    }})
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ChartActivity.this, "Request submitted to accountant.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChartActivity.this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        //download pdf file from firestore
        btnDownloadIRForm.setOnClickListener(v -> {
            if (latestFormId != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUserId)
                        .collection("irForms")
                        .document(latestFormId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists() && doc.contains("pdfUrl")) {
                                String pdfUrl = doc.getString("pdfUrl");
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "No IR form PDF available.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "No available form found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserName() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvUsername.setText(doc.getString("username"));
                    }
                });
    }

    private void checkIfPdfExists() {
        btnDownloadIRForm.setVisibility(View.GONE);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("irForms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot.getDocuments()) {
                        if (doc.contains("pdfUrl")) {
                            latestFormId = doc.getId();
                            btnDownloadIRForm.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                });
    }
}