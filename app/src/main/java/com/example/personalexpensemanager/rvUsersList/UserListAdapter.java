package com.example.personalexpensemanager.rvUsersList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalexpensemanager.FillIRFormActivity;
import com.example.personalexpensemanager.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ROLE_HEADER = 0;
    private static final int TYPE_USER = 1;

    private List<UserListItem> itemList;
    private OnToggleStatusListener listener;
    private boolean isAccountantView;

    public interface OnToggleStatusListener {
        void onStatusToggled(String uid, boolean enable);
    }

    public UserListAdapter(List<UserListItem> itemList, OnToggleStatusListener listener, boolean isAccountantView) {
        this.itemList = itemList;
        this.listener = listener;
        this.isAccountantView = isAccountantView;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof UserListItem.RoleHeader) return TYPE_ROLE_HEADER;
        else return TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ROLE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_role_header, parent, false);
            return new RoleHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_user_list, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RoleHeaderViewHolder) {
            ((RoleHeaderViewHolder) holder).bind((UserListItem.RoleHeader) itemList.get(position));
        } else if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind((UserListItem.UserItem) itemList.get(position), listener, isAccountantView);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class RoleHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoleHeader;

        public RoleHeaderViewHolder(View itemView) {
            super(itemView);
            tvRoleHeader = itemView.findViewById(R.id.tv_user_role_header);
        }

        public void bind(UserListItem.RoleHeader header) {
            tvRoleHeader.setText(header.role);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ToggleButton toggleButton;
        Button btnFillIrForm;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            toggleButton = itemView.findViewById(R.id.tv_toggle_status);
            btnFillIrForm = itemView.findViewById(R.id.btn_fill_ir_form);
        }

        public void bind(UserListItem.UserItem user, OnToggleStatusListener listener, boolean isAccountantView) {
            tvUsername.setText(user.username);

            //hide enable/disable button on accountantDashboard page
            if (isAccountantView){
                toggleButton.setVisibility(View.GONE);
            } else {
                // Admin: show and handle toggle button
                toggleButton.setVisibility(View.VISIBLE);
                toggleButton.setChecked(user.enabled);
                toggleButton.setText(user.enabled ? "Enabled" : "Disabled");

                //set button behavior
                toggleButton.setOnClickListener(v -> {
                    boolean newState = toggleButton.isChecked();
                    toggleButton.setText(newState ? "Enabled" : "Disabled");

                    // Show toast message with username and new state
                    String message = user.username + " has been " + (newState ? "enabled" : "disabled");
                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();

                    // Notify listener to update Firestore
                    listener.onStatusToggled(user.uid, newState);
                });
            }

            // Show or hide IR Form button based on role
            if (isAccountantView) {
                btnFillIrForm.setVisibility(View.VISIBLE);
                btnFillIrForm.setOnClickListener(v -> {
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, FillIRFormActivity.class);
                    intent.putExtra("userId", user.uid);
                    context.startActivity(intent);
                });
            } else {
                btnFillIrForm.setVisibility(View.GONE);
            }
        }
    }

}
