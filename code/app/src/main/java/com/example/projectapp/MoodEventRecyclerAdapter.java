package com.example.projectapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class MoodEventRecyclerAdapter extends RecyclerView.Adapter<MoodEventRecyclerAdapter.ViewHolder> {

    private List<MoodEvent> moodEvents;
    private final Context context;
    private final OnFollowClickListener followListener;

    public interface OnFollowClickListener {
        void onFollowClick(MoodEvent event);
    }

    public MoodEventRecyclerAdapter(Context context, List<MoodEvent> moodEvents, OnFollowClickListener listener) {
        this.context = context;
        this.moodEvents = moodEvents;
        this.followListener = listener;
    }

    public void setData(List<MoodEvent> newData) {
        this.moodEvents = newData;
        notifyDataSetChanged();
    }

    public void switchTab(int tabIndex, List<MoodEvent> newEvents) {
        this.moodEvents = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.public_mood_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return (moodEvents != null) ? moodEvents.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, moodText, reasonText, socialText, timeText, locationText;
        ImageView photoImage;
        Button followButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            moodText = itemView.findViewById(R.id.text_mood);
            reasonText = itemView.findViewById(R.id.text_reason);
            socialText = itemView.findViewById(R.id.text_social_situation);
            timeText = itemView.findViewById(R.id.text_time);
            locationText = itemView.findViewById(R.id.text_location);
            photoImage = itemView.findViewById(R.id.image_photo);
            followButton = itemView.findViewById(R.id.button_follow);
        }

        void bind(MoodEvent event) {
            // Set up ProfileProvider listener for username
            ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
            provider.listenForUpdates(new ProfileProvider.DataStatus() {
                @Override
                public void onDataUpdated() {
                    UserProfile userProfile = provider.getProfileByUID(event.getUserId());
                    if (userProfile != null && userProfile.getUsername() != null) {
                        usernameText.setText("@" + userProfile.getUsername());
                    } else {
                        usernameText.setText("@N/A");
                    }

                    // Set click listener for username to navigate to ProfileActivity
                    usernameText.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("uid", event.getUserId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(intent);
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e("DB Error", "Error getting username: " + error);
                    usernameText.setText("@N/A");
                }
            });

            // Display mood
            moodText.setText("Mood: " + (event.getEmotionalState() != null ? event.getEmotionalState() : "N/A"));

            // Display reason
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));

            // Display social situation
            socialText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));

            // Display time
            timeText.setText("Time: " + (event.getDate() != null ? getRelativeTime(event.getDate()) : "N/A"));

            // Display location
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            locationText.setText((lat == 0.0 && lng == 0.0) ? "Location: N/A" : "Location: " + lat + ", " + lng);

            // Display photo
            if (event.getPhotoUri() != null) {
                Glide.with(context)
                        .load(event.getPhotoUri())
                        .placeholder(android.R.color.darker_gray)
                        .into(photoImage);
            } else {
                photoImage.setVisibility(View.GONE);
            }

            // Follow button
            followButton.setOnClickListener(v -> {
                if (followListener != null) {
                    followListener.onFollowClick(event);
                }
            });
        }

        private String getRelativeTime(Date date) {
            long diffMillis = System.currentTimeMillis() - date.getTime();
            long diffMinutes = diffMillis / 60000;
            if (diffMinutes < 1) return "Just now";
            else if (diffMinutes < 60) return diffMinutes + " minutes ago";
            else return date.toString();
        }
    }
}