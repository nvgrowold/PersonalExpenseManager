package com.example.personalexpensemanager.utility;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatConverter {
    public static String formatDate(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());
        return sdf.format(date); // e.g. "Mon 24 Mar"
    }
}
