package com.example.personalexpensemanager.rvUsersList;

public class UserListItem {
    public static class RoleHeader extends UserListItem {
        public String role;
        public RoleHeader(String role) {
            this.role = role;
        }
    }

    public static class UserItem extends UserListItem {
        public String uid;
        public String username;
        public String role;
        public boolean enabled;

        public UserItem(String uid, String username, String role, boolean enabled) {
            this.uid = uid;
            this.username = username;
            this.role = role;
            this.enabled = enabled;
        }
    }
}
