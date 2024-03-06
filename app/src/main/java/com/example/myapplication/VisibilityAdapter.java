package com.example.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Set;


public class VisibilityAdapter extends ArrayAdapter<SimpleUser> {
    private List<SimpleUser> userList;
    private List<String> visibleUsers;
    private String selectedUsername;

    public VisibilityAdapter(Context context, List<SimpleUser> userList, List<String> visibleUsers) {
        super(context, android.R.layout.simple_list_item_1, userList);
        this.userList = userList;
        this.visibleUsers = visibleUsers;
    }

    public void setSelectedUsername(String selectedUsername) {
        this.selectedUsername = selectedUsername;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView textView = view.findViewById(android.R.id.text1);
        SimpleUser user = userList.get(position);

        if (user.getEmail().equals(selectedUsername)) {
            // Selected user: Black color
            textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        } else if (visibleUsers.contains(user.getEmail())) {
            // User can see the route: Green color
            textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
        } else {
            // User cannot see the route: Red color
            textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        }

        return view;
    }
}
