// -----------------------------------------------------------------------------
// File: MoodEventRecyclerAdapter.java
// -----------------------------------------------------------------------------
// This file defines the MoodEventRecyclerAdapter class, a custom RecyclerView
// Adapter for displaying MoodEvent objects in a list within the ProjectApp.
// It supports two tabs ("For You" and "Following") for switching between event
// lists and provides click listeners for editing and deleting mood events.
//
// Design Pattern: Adapter
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------

package com.example.projectapp;

import static androidx.core.content.ContextCompat.startActivity;

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

/**
 * Shows a list of MoodEvent objects (public) in a RecyclerView
 */
public class MoodEventRecyclerAdapter extends
        RecyclerView.Adapter<MoodEventRecyclerAdapter.ViewHolder> {

    private List<MoodEvent> moodEvents;
    private final Context context;
    private final OnFollowClickListener followListener;

    public interface OnFollowClickListener {
        void onFollowClick(MoodEvent event);
    }

    public MoodEventRecyclerAdapter(
            Context context,
            List<MoodEvent> moodEvents,
            OnFollowClickListener listener
    ) {
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
    public MoodEventRecyclerAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.public_mood_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MoodEventRecyclerAdapter.ViewHolder holder, int position
    ) {
        MoodEvent event = moodEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return (moodEvents != null) ? moodEvents.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, moodText, reasonText, socialText, timeText;
        TextView locationText; // Add this for location
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

            // Make sure you have this in public_mood_item.xml
            locationText = itemView.findViewById(R.id.text_location);
        }

        void bind(MoodEvent event) {
            // Display user (if you only have userId, that'll show the UID)
            // If event stores an actual username field, use event.getUsername() instead
            ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
            provider.listenForUpdates(new ProfileProvider.DataStatus() {
                @Override
                public void onDataUpdated() {
                    if (provider.getProfileByUID(event.getUserId()) != null){
                        String username = provider.getProfileByUID(event.getUserId()).getUsername();
                        usernameText.setText("@" + username);

                        usernameText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ProfileActivity.class);
                                intent.putExtra("uid",event.getUserId());

                                if (intent != null) {
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    context.startActivity(intent);
                                }
                            }
                        });

                    } else {
                        usernameText.setText("N/A");
                    }


                }

                @Override
                public void onError(String error) {
                    Log.e("DB Error", "Error getting username");
                }
            });

            // Display mood
            moodText.setText("Mood: " + (event.getEmotionalState() != null
                    ? event.getEmotionalState()
                    : "N/A"));

            // Display reason
            reasonText.setText("Reason: " + (event.getReason() != null
                    ? event.getReason()
                    : "N/A"));

            // Display social situation
            socialText.setText("Social: " + (event.getSocialSituation() != null
                    ? event.getSocialSituation()
                    : "N/A"));

            // Display time
            if (event.getDate() != null) {
                timeText.setText("Time: " + getRelativeTime(event.getDate()));
            } else {
                timeText.setText("Time: N/A");
            }

            // Display location if lat/long != 0
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            if (lat == 0.0 && lng == 0.0) {
                locationText.setText("Location: N/A");
            } else {
                locationText.setText("Location: " + lat + ", " + lng);
            }

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
