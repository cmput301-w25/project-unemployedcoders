package com.example.projectapp.views.adapters;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectapp.R;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.models.Comment;
import com.example.projectapp.models.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.prefs.PreferencesFactory;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private List<Comment> comments;
    private Context context;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public CommentAdapter(Context context, List<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
        this.comments = comments;
    }

    static class CommentViewHolder {
        TextView usernameText, commentText, timestampText;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CommentViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
            holder = new CommentViewHolder();
            holder.usernameText = convertView.findViewById(R.id.comment_username);
            holder.commentText = convertView.findViewById(R.id.comment_text);
            holder.timestampText = convertView.findViewById(R.id.comment_timestamp);

            convertView.setTag(holder);
        } else {
            holder = (CommentViewHolder) convertView.getTag();
        }

        Comment comment = comments.get(position);
        holder.usernameText.setText("@" + comment.getCommenterUsername());
        holder.commentText.setText(comment.getText());
        holder.timestampText.setText(sdf.format(comment.getTimestamp()));

        ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                UserProfile prof = provider.getProfileByUID(comment.getCommenterUid());
                holder.usernameText.setText(prof == null? "Username": prof.getUsername());
            }

            @Override
            public void onError(String error) {

            }
        });

        return convertView;
    }

}
