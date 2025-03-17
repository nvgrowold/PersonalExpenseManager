package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.personalexpensemanager.db.User;
import com.example.personalexpensemanager.db.UserDAO;
import com.example.personalexpensemanager.db.utility.UserDB;
import com.example.personalexpensemanager.db.utility.Role;


import org.mindrot.jbcrypt.BCrypt;

public class RegisterActivity extends AppCompatActivity {

    TextView tvAlreadyHaveAccount;
    Spinner roleSpinner;
    String[] userRoles;
    EditText etUsername, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    UserRoleSpinnerAdapter userRoleSpinnerAdapter;
    String selectedRole;
    UserDAO userDAO;

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

        // Initialize database and DAO
        userDAO = UserDB.getInstance(this).userDAO();

        // Navigate to login screen when "Already have an account?" is clicked
        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //     // ============================
        //        // ROLE SELECTION DROPDOWN (SPINNER)
        //        // ============================
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
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_LONG).show();
                    return;
                }

                //check if user email already exists
                User existingUserEmail = userDAO.getUserByEmail(email);
                if (existingUserEmail != null) {
                    Toast.makeText(RegisterActivity.this, "User email already exists!", Toast.LENGTH_LONG).show();
                    return;
                }
                //check if username already exists
                User existingUsername = userDAO.getUserByUsername(email);
                if (existingUsername != null) {
                    Toast.makeText(RegisterActivity.this, "Username already exists!", Toast.LENGTH_LONG).show();
                    return;
                }

                // CONVERT ROLE STRING TO ENUM
                Role userRole;
                try{
                    userRole = Role.valueOf(selectedRole.toUpperCase());// Convert String to Enum
                } catch (IllegalArgumentException e) {
                    Toast.makeText(RegisterActivity.this, "Invalid role selection!", Toast.LENGTH_LONG).show();
                    return;
                }

                // HASH PASSWORD BEFORE STORING
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // CREATE NEW USER OBJECT & INSERT INTO DATABASE
                //add new user
                User newUser = new User(username, email, hashedPassword, userRole, "0.00");
                userDAO.insertUser(newUser);

                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                //navigate to Edit User page
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish(); // Close registration activity
            }
        });
    }
}