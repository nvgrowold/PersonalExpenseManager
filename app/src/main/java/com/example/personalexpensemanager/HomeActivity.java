package com.example.personalexpensemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    ImageView ivGif_Money;
    Button btnStart;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivGif_Money = findViewById(R.id.IV_gif_money_flying);
        btnStart = findViewById(R.id.btn_get_started);
        auth = FirebaseAuth.getInstance();

        // 🔒 Force logout from Firebase
        FirebaseAuth.getInstance().signOut();

        // 🔒 Force logout from Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Optional: Show toast
            // Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        });


        // ✅ Ensure Google Play Services is available
        checkGooglePlayServices();

        //play home page gif
        Glide.with(HomeActivity.this).load(R.drawable.gif_money_flying).into(ivGif_Money);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to check if Google Play Services is available
    private void checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int status = apiAvailability.isGooglePlayServicesAvailable(this);

        if (status != ConnectionResult.SUCCESS) {
            apiAvailability.makeGooglePlayServicesAvailable(this);
            Toast.makeText(this, "Google Play Services is required for Firebase Authentication", Toast.LENGTH_LONG).show();
        }
    }
}