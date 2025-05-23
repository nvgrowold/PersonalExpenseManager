package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.personalexpensemanager.utility.BottomNavHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class UserProfileActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView ivAvatar;
    Button btnEditProfile, btnSettings, btnLogout;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvUsername = findViewById(R.id.tv_username);
        ivAvatar = findViewById(R.id.iv_avatar2);
        btnEditProfile = findViewById(R.id.btn_request_service);
        btnSettings = findViewById(R.id.btn_download_filled_IR_form);
        btnLogout = findViewById(R.id.btn_logout);

        user = FirebaseAuth.getInstance().getCurrentUser();

        //call helper class to set the bottom navi bar
        BottomNavHelper.setupBottomNav(this);

        //load user data from firstore
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            tvUsername.setText(username != null ? username : "User");

                            if (user.getPhotoUrl() != null) {
                                Glide.with(this)
                                        .load(user.getPhotoUrl())
                                        .placeholder(R.drawable.icon_person_login)
                                        .circleCrop()
                                        .into(ivAvatar);
                            } else {
                                ivAvatar.setImageResource(R.drawable.icon_person_login);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUsername.setText("User");
                        ivAvatar.setImageResource(R.drawable.icon_person_login);
                    });
        } else {
            tvUsername.setText("User");
            ivAvatar.setImageResource(R.drawable.icon_person_login);
        }

        //Initialize the client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);


        //set logout button click event
        //also log out google sign in
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}