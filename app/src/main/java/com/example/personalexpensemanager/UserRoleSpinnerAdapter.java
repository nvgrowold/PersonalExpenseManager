package com.example.personalexpensemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

//a custom spinner adapter class for Android Spinner
// to display user icons and roles in the dropdown menu
public class UserRoleSpinnerAdapter extends BaseAdapter {

    //final: means the value of the variable cannot be changed after initialisation, prevents accidental modifications, good practice
    final Context context; //to store the context(activity or fragment) to access resources for later use
    final String[] roles;

    //user icons array, use int[] because Resources IDs in Android are integers
    final int[] userIcons = {R.drawable.icon_person_login, R.drawable.icon_admin, R.drawable.icon_accountant};

    ImageView ivIcon;
    TextView tvRole;

    //constructor to initialise the adapter
    public UserRoleSpinnerAdapter(Context context, String[] roles) {
        this.context = context;
        this.roles = roles;
    }

    @Override
    public int getCount() {
        return roles.length;
    }

    @Override
    public Object getItem(int position) {
        return roles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //creating the View
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {//position: index of displaying item;
                                                                            // convertView: reusable view to save memory
                                                                            // parent: all spinner items.
        if(convertView == null){ //if null, then create a new view from spinner_item.xml
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        //find the corresponding UI components
        ivIcon = convertView.findViewById(R.id.roleIcon);
        tvRole = convertView.findViewById(R.id.roleName);

        //set the icon and role with icon array and role array
        ivIcon.setImageResource(userIcons[position]);
        tvRole.setText(roles[position]);

        return convertView; //return the customised convertView to display inside the Spinner
    }

    //each dropdown items need their own view, reuse the getView() method to provide the same layout for each options
    //recycling views
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
