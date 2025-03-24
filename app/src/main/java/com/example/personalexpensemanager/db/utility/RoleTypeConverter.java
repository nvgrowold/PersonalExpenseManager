package com.example.personalexpensemanager.db.utility;

import androidx.room.TypeConverter;


// ============================
// TYPE CONVERTERS FOR Role
// ============================
public class RoleTypeConverter {
    @TypeConverter
    public static Role StringToValue(String value){
        return value == null ? null : Role.valueOf(value);
    }

    @TypeConverter
    public static String roleToString(Role role){
        return role == null ? null : role.name();
    }
}
