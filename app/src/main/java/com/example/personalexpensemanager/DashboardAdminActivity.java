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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardAdminActivity extends AppCompatActivity {

    TextView tvHelloAdmin, tvUserNum;
    ImageView avatarView;
    FirebaseUser user;
    Button btnLogout;
    RecyclerView rv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvHelloAdmin = findViewById(R.id.tv_hello_user_admin);
        tvUserNum = findViewById(R.id.tv_total_users_number);
        avatarView = findViewById(R.id.iv_avatar_admin);
        btnLogout = findViewById(R.id.btn_logout_admin);
        rv = findViewById(R.id.rv_users);

        //must initialise before retrieving
        user = FirebaseAuth.getInstance().getCurrentUser();

        //load admin user information
        if(user != null){
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String username = documentSnapshot.getString("username");
                            tvHelloAdmin.setText("Hello, " + username);
                            //load user icon/photo from firestore
                            if (user.getPhotoUrl() != null) {
                                Glide.with(this)
                                        .load(user.getPhotoUrl())
                                        .placeholder(R.drawable.icon_person_login) // fallback image
                                        .circleCrop()
                                        .into(avatarView);
                            } else {
                                // fallback in case no photo
                                avatarView.setImageResource(R.drawable.icon_person_login);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvHelloAdmin.setText("Hello, User");
                        avatarView.setImageResource(R.drawable.icon_person_login);
                    });
            }

        //set logout button click event
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DashboardAdminActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        //load total number of users
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    tvUserNum.setText(String.valueOf(totalUsers));
                })
                .addOnFailureListener(e -> {
                    tvUserNum.setText("0");
                });

    }
}