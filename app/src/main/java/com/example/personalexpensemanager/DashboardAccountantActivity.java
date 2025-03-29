package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.personalexpensemanager.rvUsersList.UserListAdapter;
import com.example.personalexpensemanager.rvUsersList.UserListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DashboardAccountantActivity extends AppCompatActivity {
    TextView tvHelloAccountant, tvUserNum;
    ImageView avatarView;
    FirebaseUser user;
    Button btnLogout;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_accountant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHelloAccountant = findViewById(R.id.tv_hello_user_accountant);
        avatarView = findViewById(R.id.iv_avatar_accountant);
        tvUserNum = findViewById(R.id.tv_total_users_number);
        btnLogout = findViewById(R.id.btn_logout_accountant);
        rv = findViewById(R.id.rv_users);
        rv.setLayoutManager(new LinearLayoutManager(this));

        //must initialise before retrieving
        user = FirebaseAuth.getInstance().getCurrentUser();

        //load accountant user information
        if(user != null){
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String username = documentSnapshot.getString("username");
                            tvHelloAccountant.setText("Hello, " + username);
                            //load user icon/photo from firestore
                            if (user.getPhotoUrl() != null) {
                                Glide.with(this)
                                        .load(user.getPhotoUrl())
                                        .placeholder(R.drawable.icon_accountant) // fallback image
                                        .circleCrop()
                                        .into(avatarView);
                            } else {
                                // fallback in case no photo
                                avatarView.setImageResource(R.drawable.icon_accountant);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvHelloAccountant.setText("Hello, User");
                        avatarView.setImageResource(R.drawable.icon_accountant);
                    });
        }
        //set logout button click event
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DashboardAccountantActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        //load total number of users
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = 0;
                    for (var doc : queryDocumentSnapshots.getDocuments()){
                        String role = doc.getString("role");
                        if (role != null && role.equalsIgnoreCase("user")){
                            totalUsers++;
                        }
                    }
                    tvUserNum.setText(String.valueOf(totalUsers));
                })
                .addOnFailureListener(e -> {
                    tvUserNum.setText("0");
                });

        //Populate the RecyclerView with users list
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<UserListItem> userList = new java.util.ArrayList<>();

                    for (var doc : querySnapshot.getDocuments()) {
                        String uid = doc.contains("firebaseUid") ? doc.getString("firebaseUid") : doc.getId();
                        String username = doc.getString("username");
                        String role = doc.getString("role");
                        boolean enabled = doc.getBoolean("enabled") != null && doc.getBoolean("enabled");

                        Log.d("AccountantDashboard", "Fetched user: uid=" + uid + ", role=" + role + ", username=" + username);

                        // ✅ Only include users with role "user"
                        if (role != null && "user".equalsIgnoreCase(role)) {
                            UserListItem.UserItem userItem = new UserListItem.UserItem(uid, username, role, enabled);
                            userList.add(userItem);
                        }
                    }

                    UserListAdapter adapter = new UserListAdapter(userList, (uid, enable) -> {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update("enabled", enable);
                    }, true); //true flag because this is the accountant dashboard

                    rv.setAdapter(adapter);
                });

    }
}