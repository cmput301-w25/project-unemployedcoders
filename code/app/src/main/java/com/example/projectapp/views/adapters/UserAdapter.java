package com.example.projectapp.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.R;
import com.example.projectapp.models.UserProfile;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<UserProfile> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserProfile user);
    }

    public UserAdapter(Context context, List<UserProfile> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserProfile user = users.get(position);
        holder.nameText.setText(user.getName());
        holder.usernameText.setText("@" + user.getUsername());
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<UserProfile> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, usernameText;

        UserViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            usernameText = itemView.findViewById(R.id.username_text);
        }
    }
}