package com.example.personalexpensemanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.personalexpensemanager.transaction.DateHeader;
import com.example.personalexpensemanager.transaction.Transaction;
import com.example.personalexpensemanager.transaction.TransactionAdapter;
import com.example.personalexpensemanager.transaction.TransactionItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    TextView tvHelloUser;
    ShapeableImageView avatarView;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvHelloUser = findViewById(R.id.tv_hello_user);
        avatarView = findViewById(R.id.iv_avatar);
        user = FirebaseAuth.getInstance().getCurrentUser();
        //call helper class to set the bottom navi bar
        BottomNavHelper.setupBottomNav(this);

        if(user != null){
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String username = documentSnapshot.getString("username");
                            tvHelloUser.setText("Hello, " + username);
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
                        tvHelloUser.setText("Hello, User");
                        avatarView.setImageResource(R.drawable.icon_person_login);
                    });
        }


        //load recent transactions to recyclerView
        RecyclerView rv = findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<TransactionItem> items = new ArrayList<>();
        items.add(new DateHeader("Sun 23 Mar"));
        items.add(new Transaction("uid", "Groceries", "GRACE JOEL", "desc", "expense", 4.50, null));
        items.add(new DateHeader("Sat 22 Mar"));
        items.add(new Transaction("uid", "Grocery", "New World", "desc", "expense", 51.83, null));
        items.add(new Transaction("uid", "Savings", "Rapid Save", "desc", "expense", 100.00, null));

        TransactionAdapter adapter = new TransactionAdapter(items);
        rv.setAdapter(adapter);
    }
}