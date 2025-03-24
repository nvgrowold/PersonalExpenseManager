package com.example.personalexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
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
import com.example.personalexpensemanager.transaction.DateHeader;
import com.example.personalexpensemanager.transaction.Transaction;
import com.example.personalexpensemanager.transaction.TransactionAdapter;
import com.example.personalexpensemanager.transaction.TransactionItem;
import com.example.personalexpensemanager.utility.BottomNavHelper;
import com.example.personalexpensemanager.utility.DateFormatConverter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    TextView tvHelloUser, tvBalance, tvIncome, tvExpense;
    ImageView avatarView;
    FirebaseUser user;
    ImageButton imageButtonAdd;

    RecyclerView rv;
    TransactionAdapter adapter;
    List<TransactionItem> items;

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
        imageButtonAdd = findViewById(R.id.imageButton_add);
        tvBalance = findViewById(R.id.tv_balance_amount);
        tvIncome = findViewById(R.id.tv_income_amount);
        tvExpense = findViewById(R.id.tv_expense_amount);

        //call helper class to set the bottom navi bar
        BottomNavHelper.setupBottomNav(this);

        //caculate user account banlance
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction tx = doc.toObject(Transaction.class);

                        if ("income".equalsIgnoreCase(tx.getTransactionType())) {
                            totalIncome += tx.getAmount(); // positive
                        } else if ("expense".equalsIgnoreCase(tx.getTransactionType())) {
                            totalExpense += Math.abs(tx.getAmount()); // ensure positive value for display
                        }
                    }

                    double balance = totalIncome - totalExpense;

                    tvBalance.setText(String.format("$%,.2f", balance));
                    tvIncome.setText(String.format("$%,.2f", totalIncome));
                    tvExpense.setText(String.format("-$%,.2f", totalExpense));
                })
                .addOnFailureListener(e -> {
                    tvBalance.setText("$0.00");
                    tvIncome.setText("$0.00");
                    tvExpense.setText("-$0.00");
                });



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
        rv = findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
//        items.add(new DateHeader("Sun 23 Mar"));
//        items.add(new Transaction("uid", "Groceries", "GRACE JOEL", "desc", "expense", 4.50, null));
//        items.add(new DateHeader("Sat 22 Mar"));
//        items.add(new Transaction("uid", "Grocery", "New World", "desc", "expense", 51.83, null));
//        items.add(new Transaction("uid", "Savings", "Rapid Save", "desc", "expense", 100.00, null));
        adapter = new TransactionAdapter(items, transaction -> {
            Intent intent = new Intent(DashboardActivity.this, TransactionViewDetailActivity.class);
            intent.putExtra("transactionId", transaction.getTid());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
//        //load user transaction data from firebase
//        FirebaseFirestore.getInstance()
//                .collection("users")
//                .document(user.getUid())
//                .collection("transactions")
//                .orderBy("date", Query.Direction.DESCENDING)
//                .limit(12)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    String lastDate = "";
//
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                        Transaction tx = doc.toObject(Transaction.class);
//
//                        String formattedDate = DateFormatConverter.formatDate(tx.getDate()); // Utility function (next)
//                        if (!formattedDate.equals(lastDate)) {
//                            items.add(new DateHeader(formattedDate));
//                            lastDate = formattedDate;
//                        }
//
//                        items.add(tx);
//                    }
//
//                    adapter.notifyDataSetChanged();
//                });

        //'add' button click animation effect
        imageButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation bounce = AnimationUtils.loadAnimation(DashboardActivity.this, R.anim.click_bounce);
                v.startAnimation(bounce);

                Intent intent = new Intent(DashboardActivity.this, TransactionAddNewActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh account balance when return from add/edit/delete transactions
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction tx = doc.toObject(Transaction.class);

                        if ("income".equalsIgnoreCase(tx.getTransactionType())) {
                            totalIncome += tx.getAmount(); // positive
                        } else if ("expense".equalsIgnoreCase(tx.getTransactionType())) {
                            totalExpense += Math.abs(tx.getAmount());
                        }
                    }

                    double balance = totalIncome - totalExpense;

                    tvBalance.setText(String.format("$%,.2f", balance));
                    tvIncome.setText(String.format("$%,.2f", totalIncome));
                    tvExpense.setText(String.format("-$%,.2f", totalExpense));
                })
                .addOnFailureListener(e -> {
                    tvBalance.setText("$0.00");
                    tvIncome.setText("$0.00");
                    tvExpense.setText("-$0.00");
                });

        // Refresh latest 12 transactions
        items.clear(); // clear old items first
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(12)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String lastDate = "";

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction tx = doc.toObject(Transaction.class);
                        String formattedDate = DateFormatConverter.formatDate(tx.getDate());

                        if (!formattedDate.equals(lastDate)) {
                            items.add(new DateHeader(formattedDate));
                            lastDate = formattedDate;
                        }

                        items.add(tx);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

}