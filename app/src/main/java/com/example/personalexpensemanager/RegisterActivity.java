package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personalexpensemanager.utility.InputHintRemover;
import com.example.personalexpensemanager.utility.UserRoleSpinnerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// Import password hashing library
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextView tvAlreadyHaveAccount;
    Spinner roleSpinner;
    String[] userRoles;
    EditText etUsername, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    UserRoleSpinnerAdapter userRoleSpinnerAdapter;
    String selectedRole;
//    UserDAO userDAO;
    FirebaseAuth auth; // Firebase Authentication
    FirebaseFirestore db; // Firestore Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etUsername = findViewById(R.id.edit_text_username);
        etEmail = findViewById(R.id.edit_text_email);
        etPassword = findViewById(R.id.edit_text_password);
        etConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvAlreadyHaveAccount = findViewById(R.id.text_view_already_have_account);

        //set input fields' Hint behavior, when user click hint gone
        InputHintRemover.setHintBehavior(etUsername, "Username");
        InputHintRemover.setHintBehavior(etEmail, "Email");
        InputHintRemover.setHintBehavior(etPassword, "******");
        InputHintRemover.setHintBehavior(etConfirmPassword, "******");

        //Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Navigate to login screen when "Already have an account?" is clicked
        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // ============================
        //  ROLE SELECTION DROPDOWN (SPINNER)
        // ============================
        roleSpinner = findViewById(R.id.spinner_user_role);
        userRoles = getResources().getStringArray(R.array.userRoles_array);
        userRoleSpinnerAdapter = new UserRoleSpinnerAdapter(RegisterActivity.this, userRoles);
        roleSpinner.setAdapter(userRoleSpinnerAdapter);
        //handle selected role
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // ============================
        // REGISTER BUTTON CLICK HANDLER
        // ============================
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                //user input validation
                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (password.length() < 6) {  // Password length validation
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_LONG).show();
                    return;
                }

                //check if user email already exists in Firestore
                db.collection("users").whereEqualTo("email", email).get() //Queries Firestore’s "users" collection.
                                                                                          //Checks if there is already a user where the email matches the entered email.
                        .addOnSuccessListener(querySnapshot -> {
                               //query result is NOT empty
                            if(!querySnapshot.isEmpty()){ // If Firestore finds a user with the same email
                                Toast.makeText(RegisterActivity.this, "User email already exists!", Toast.LENGTH_LONG).show();
                            } else { //If the email does not exist in Firestore, the app creates a new user.
                                registerUser(username, email, password, selectedRole);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error checking user: " + e.getMessage(), e);
                            Toast.makeText(RegisterActivity.this, "Error checking users: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }
    // ============================
    // REGISTER USER IN FIREBASE AUTH & STORE IN FIRESTORE
    // ============================
    private void registerUser(String username, String email, String password, String role){
        auth.createUserWithEmailAndPassword(email,password) //Registers the user in Firebase Authentication.
                .addOnCompleteListener(task -> { //if create user complete, run this. Executes after Firebase finishes user creation.
                        if(task.isSuccessful()) { //if created user successfully, run this
                            String firebaseUid = auth.getCurrentUser().getUid();
                            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());  // Hash password before storing

                            //create user data map
                            Map<String, Object> user = new HashMap<>(); //HashMap<> is a data structure used to store key-value pairs, like a dictionary.
                            user.put("firebaseUid", firebaseUid);
                            user.put("username", username);
                            user.put("email", email);
                            user.put("password", hashedPassword);
                            user.put("role", role.toUpperCase());
                            user.put("balance", "0.00");
                            user.put("enabled", true);


                            // Store user in firestore
                            /**db.collection("users") → Get the "users" collection in Firestore.
                             .document(firebaseUid) → Create a document for the user (uses UID as the document name).
                             .set(user) → Save the user HashMap data inside Firestore.**/
                            db.collection("users").document(firebaseUid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {//if query success, run this
                                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    //if query fail, run this
                                    .addOnFailureListener(e ->
                                            Toast.makeText(RegisterActivity.this, "Error saving user data!", Toast.LENGTH_LONG).show());
                        } else { //if create user fail, run this
                                Toast.makeText(RegisterActivity.this, "Authentication Failed!", Toast.LENGTH_LONG).show();
                        }
                });

    }
//
//    //remove hint when user click and type in input field
//    private void setHintBehavior(EditText editText, String hintText){
//        editText.setOnFocusChangeListener((v, hasFocus) -> {
//            if(hasFocus){
//                editText.setHint("");
//            } else {
//                if (editText.getText().toString().isEmpty()){
//                    editText.setHint(hintText);
//                }
//            }
//        });
//    }
}