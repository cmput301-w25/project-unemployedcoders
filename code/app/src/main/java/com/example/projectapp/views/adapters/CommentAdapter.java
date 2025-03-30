package com.example.projectapp.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectapp.R;
import com.example.projectapp.models.Comment;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private Context context;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.usernameText.setText("@" + comment.getCommenterUsername());
        holder.commentText.setText(comment.getText());
        holder.timestampText.setText(sdf.format(comment.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, commentText, timestampText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.comment_username);
            commentText = itemView.findViewById(R.id.comment_text);
            timestampText = itemView.findViewById(R.id.comment_timestamp);
        }
    }
}
