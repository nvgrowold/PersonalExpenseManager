package com.example.personalexpensemanager.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.personalexpensemanager.R;

public class TypeSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final String[] types;
    private final int[] icons = {
            R.drawable.icon_arrow_circle_down,  // Income
            R.drawable.icon_arrow_circle_up     // Expense
    };

    public TypeSpinnerAdapter(Context context, String[] types) {
        this.context = context;
        this.types = types;
    }

    @Override
    public int getCount() {
        return types.length;
    }

    @Override
    public Object getItem(int position) {
        return types[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_transaction_type_item, parent, false);

        ImageView icon = convertView.findViewById(R.id.transactionIcon);
        TextView name = convertView.findViewById(R.id.typeName);

        icon.setImageResource(icons[position % icons.length]);
        name.setText(types[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}

