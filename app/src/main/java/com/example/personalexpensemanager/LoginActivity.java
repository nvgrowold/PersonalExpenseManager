package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    TextView tv_register, tv_forgotPassword;
    EditText etEmail, etPassword;
    Button btnLogin;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin = findViewById(R.id.btn_Login);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_Password);
        tv_forgotPassword = findViewById(R.id.text_view_forgot_password);
        tv_register = findViewById(R.id.text_view_register);

        //set input fields' Hint behavior, when user click hint gone
        setHintBehavior(etEmail, "Email");
        setHintBehavior(etPassword, "******");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = etEmail.getText().toString();
                String passwordInput = etPassword.getText().toString();

                //user input validation
                if(emailInput.isEmpty() || passwordInput.isEmpty()){
                    Toast.makeText(LoginActivity.this, "All fields are required!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (passwordInput.length() < 6) {  // Password length validation
                    Toast.makeText(LoginActivity.this, "Password must be at least 6 characters!", Toast.LENGTH_LONG).show();
                    return;
                }
                //check if user email exists in Firestore
                db.collection("users").whereEqualTo("email", emailInput).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if(queryDocumentSnapshots.isEmpty()){
                                Toast.makeText(LoginActivity.this, "User email does not exist!", Toast.LENGTH_LONG).show();
                            } else {
                                loginUser(emailInput, passwordInput);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error checking user: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Error checking users: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            //Login user in firebase with email and password
            private void loginUser(String emailInput, String passwordInput) {
                auth.signInWithEmailAndPassword(emailInput, passwordInput)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                String userID = auth.getCurrentUser().getUid();
                                db.collection("users").document(userID).get()
                                                .addOnSuccessListener(documentSnapshot -> {
                                                   if(documentSnapshot.exists()){
                                                       String role = documentSnapshot.getString("role");
                                                       if ("Admin".equalsIgnoreCase(role)){
                                                           intent = new Intent(LoginActivity.this, DashboardAdminActivity.class);
                                                       }
                                                       else if ("Accountant".equalsIgnoreCase(role)){
                                                           intent = new Intent(LoginActivity.this, DashboardAccountantActivity.class);
                                                       } else{
                                                           intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                       }
                                                       Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                                                       startActivity(intent);
                                                       finish();
                                                   }else {
                                                       Toast.makeText(LoginActivity.this, "User data not found!", Toast.LENGTH_LONG).show();
                                                   }
                                                })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error fetching user role: " + e.getMessage(), e);
                                            Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    //remove hint when user click and type in input field
    private void setHintBehavior(EditText editText, String hintText){
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                editText.setHint("");
            } else {
                if (editText.getText().toString().isEmpty()){
                    editText.setHint(hintText);
                }
            }
        });
    }
}