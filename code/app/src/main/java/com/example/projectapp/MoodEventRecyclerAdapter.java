package com.example.projectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import com.bumptech.glide.Glide;


public class MoodEventRecyclerAdapter extends RecyclerView.Adapter<MoodEventRecyclerAdapter.ViewHolder> {

    private List<MoodEvent> moodEvents;
    private Context context;
    private OnFollowClickListener followListener;

    public interface OnFollowClickListener {
        void onFollowClick(MoodEvent event);
    }

    public MoodEventRecyclerAdapter(Context context, List<MoodEvent> moodEvents, OnFollowClickListener listener) {
        this.context = context;
        this.moodEvents = moodEvents;
        this.followListener = listener;
    }

    // Update data in the adapter
    public void setData(List<MoodEvent> newData) {
        this.moodEvents = newData;
        notifyDataSetChanged();
    }

    public void switchTab(int tabIndex, List<MoodEvent> newEvents) {
        // Update internal state (you can use tabIndex if needed)
        this.moodEvents = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoodEventRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.public_mood_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodEventRecyclerAdapter.ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return (moodEvents != null) ? moodEvents.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, moodText, reasonText, socialText, timeText;
        ImageView photoImage;
        Button followButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            moodText = itemView.findViewById(R.id.text_mood);
            reasonText = itemView.findViewById(R.id.text_reason);
            socialText = itemView.findViewById(R.id.text_social_situation);
            timeText = itemView.findViewById(R.id.text_time);
            photoImage = itemView.findViewById(R.id.image_photo);
            followButton = itemView.findViewById(R.id.button_follow);
        }

        void bind(UserProfile profile) {
            // Bind text fields
            usernameText.setText(profile.getUsername() != null ? profile.getUsername() : "Unknown");}
        void bind(MoodEvent event) {
            moodText.setText("Mood: " + (event.getEmotionalState() != null ? event.getEmotionalState() : "N/A"));
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));
            socialText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));

            // Time field, e.g. "10 minutes ago"
            if (event.getDate() != null) {
                // If you have a utility method, or do a manual calculation
                timeText.setText("Time: " + getRelativeTime(event.getDate()));
            } else {
                timeText.setText("Time: N/A");
            }

            // Photo
            if (event.getPhotoUrl() != null) {
                // Use Glide or Picasso to load
                Glide.with(context)
                        .load(event.getPhotoUrl())
                        .placeholder(android.R.color.darker_gray)
                        .into(photoImage);
            } else {
                photoImage.setImageResource(android.R.color.transparent);
            }

            // Follow button
            followButton.setOnClickListener(v -> {
                if (followListener != null) {
                    followListener.onFollowClick(event);
                }
            });
        }

        private String getRelativeTime(Date date) {
            // Example: convert to "5 minutes ago"
            long diffMillis = System.currentTimeMillis() - date.getTime();
            long diffMinutes = diffMillis / 60000;
            if (diffMinutes < 1) return "Just now";
            else if (diffMinutes < 60) return diffMinutes + " minutes ago";
            else {
                // etc...
                return date.toString(); // fallback
            }
        }
    }
}

