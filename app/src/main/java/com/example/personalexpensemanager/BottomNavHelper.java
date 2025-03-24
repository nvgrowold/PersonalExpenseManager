package com.example.personalexpensemanager;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class BottomNavHelper {

    public static void setupBottomNav(Activity activity) {
        View btnHome = activity.findViewById(R.id.btn_home);
        View btnTransaction = activity.findViewById(R.id.btn_transaction);
        View btnChart = activity.findViewById(R.id.btn_chart);
        View btnUser = activity.findViewById(R.id.btn_user);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                if (!(activity instanceof DashboardActivity)) {
                    activity.startActivity(new Intent(activity, DashboardActivity.class));
                }
            });
        }

        if (btnTransaction != null) {
            btnTransaction.setOnClickListener(v -> {
                if (!(activity instanceof TransactionAllActivity)) {
                    activity.startActivity(new Intent(activity, TransactionAllActivity.class));
                }
            });
        }

        if (btnChart != null) {
            btnChart.setOnClickListener(v -> {
                if (!(activity instanceof ChartActivity)) {
                    activity.startActivity(new Intent(activity, ChartActivity.class));
                }
            });
        }

        if (btnUser != null) {
            btnUser.setOnClickListener(v -> {
                if (!(activity instanceof UserProfileActivity)) {
                    activity.startActivity(new Intent(activity, UserProfileActivity.class));
                }
            });
        }
    }
}

