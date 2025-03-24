package com.example.personalexpensemanager.db.utility;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

// ============================
// TYPE CONVERTERS FOR Decimal (Balance, amount,,,,)
// ============================
// ROOM not support BigDecimal directly

public class BigDecimalConverter {

    //string to decimal
    @TypeConverter
    public static BigDecimal stringToValue(String value){
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }


    // decimal to string for storing in ROOM
    @TypeConverter
    public  static  String decimalToString(BigDecimal bigDecimal){
        return bigDecimal == null ? "0.00" : bigDecimal.toString();
    }
}
