package com.example.personalexpensemanager.db.utility;

import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

public class TimestampConverter {

    @TypeConverter
    public static Timestamp fromLong(Long value) {
        return value == null ? null : new Timestamp(value / 1000, 0);
    }

    @TypeConverter
    public static Long timestampToLong(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toDate().getTime();
    }
}
