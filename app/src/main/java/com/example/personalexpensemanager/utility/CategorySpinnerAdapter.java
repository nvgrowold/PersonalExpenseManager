package com.example.personalexpensemanager.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.personalexpensemanager.R;

public class CategorySpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final String[] categories;
    private final int[] icons = {
            R.drawable.icon_food,
            R.drawable.icon_shopping,
            R.drawable.icon_transportation,
            R.drawable.icon_home,
            R.drawable.icon_gift,
            R.drawable.icon_salary,
            R.drawable.icon_category
    };

    public CategorySpinnerAdapter(Context context, String[] categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.length;
    }

    @Override
    public Object getItem(int position) {
        return categories[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_category_item, parent, false);

        ImageView icon = convertView.findViewById(R.id.catergoryIcon);
        TextView name = convertView.findViewById(R.id.catergoryName);

        icon.setImageResource(icons[position % icons.length]); // Fallback in case fewer icons
        name.setText(categories[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
