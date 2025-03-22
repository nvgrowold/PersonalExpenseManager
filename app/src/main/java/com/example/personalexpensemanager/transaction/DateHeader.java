package com.example.personalexpensemanager.transaction;

public class DateHeader implements TransactionItem{
    private String date;

    public DateHeader(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

}
