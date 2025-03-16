package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    TextView tvAlreadyHaveAccount;
    Spinner roleSpinner;
    String[] userRoles;

    UserRoleSpinnerAdapter userRoleSpinnerAdapter;

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

        tvAlreadyHaveAccount = findViewById(R.id.text_view_already_have_account);

        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //dropdown spinner
        roleSpinner = findViewById(R.id.spinner_user_role);
        userRoles = getResources().getStringArray(R.array.userRoles_array);

        userRoleSpinnerAdapter = new UserRoleSpinnerAdapter(RegisterActivity.this, userRoles);
        roleSpinner.setAdapter(userRoleSpinnerAdapter);

    }
}