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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;

import com.example.personalexpensemanager.rvUsersList.UserListItem;
import com.example.personalexpensemanager.rvUsersList.UserListAdapter;

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
        rv.setLayoutManager(new LinearLayoutManager(this));

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


        //Populate the RecyclerView with users list
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<UserListItem.UserItem>> groupedByRole = new TreeMap<>();

                    for (var doc : querySnapshot.getDocuments()) {
                        String uid = doc.getString("firebaseUid");
                        String username = doc.getString("username");
                        String role = doc.getString("role");
                        boolean enabled = doc.getBoolean("enabled") != null && doc.getBoolean("enabled");

                        UserListItem.UserItem userItem = new UserListItem.UserItem(uid, username, role, enabled);
                        groupedByRole.computeIfAbsent(role, k -> new java.util.ArrayList<>()).add(userItem);
                    }

                    List<UserListItem> finalList = new java.util.ArrayList<>();
                    for (String role : groupedByRole.keySet()) {
                        finalList.add(new UserListItem.RoleHeader(role));
                        finalList.addAll(groupedByRole.get(role));
                    }
                    //debug: log final users list
                    for (UserListItem item : finalList) {
                        if (item instanceof UserListItem.RoleHeader) {
                            Log.d("UserList", "Header: " + ((UserListItem.RoleHeader) item).role);
                        }
                    }

                    UserListAdapter adapter = new UserListAdapter(finalList, (uid, enable) -> {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update("enabled", enable);
                    });

                    rv.setAdapter(adapter);
                });

    }
}