package com.example.projectapp.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.R;
import com.example.projectapp.models.UserProfile;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserProfile> users;
    private Context context;

    public UserAdapter(Context context, List<UserProfile> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile user = users.get(position);
        if (user == null) {
            holder.nameText.setText("Unknown Name");
            holder.usernameText.setText("Unknown Username");
            return;
        }

        // Display name (with null check)
        String name = user.getName();
        holder.nameText.setText(name != null ? name : "No Name");

        // Display username (with null check)
        String username = user.getUsername();
        holder.usernameText.setText(username != null ? "@" + username : "No Username");
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void updateUsers(List<UserProfile> newUsers) {
        this.users.clear();
        if (newUsers != null) {
            this.users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView usernameText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            usernameText = itemView.findViewById(R.id.username_text);
        }
    }
}