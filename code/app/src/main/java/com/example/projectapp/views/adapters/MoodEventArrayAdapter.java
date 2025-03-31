package com.example.projectapp.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.MoodEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    private ArrayList<MoodEvent> events;
    private Context context;
    private OnMoodEventClickListener listener;

    /**
     * Interface for classes that use this array adapter
     */
    public interface OnMoodEventClickListener {
        void onEditMoodEvent(MoodEvent event, int position);
        void onDeleteMoodEvent(MoodEvent event, int position);
    }

    public MoodEventArrayAdapter(Context context, ArrayList<MoodEvent> events, OnMoodEventClickListener listener) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
        this.listener = listener;
    }

    static class ViewHolder {
        TextView usernameText, moodText, reasonText, socialText, timeText, locationText;
        ImageView photo;
        ConstraintLayout background; // Not used in the new layout, but kept for compatibility
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        MoodEvent moodEvent = events.get(position);
        boolean hasImage = moodEvent.getPhotoUri() != null && !moodEvent.getPhotoUri().toString().isEmpty();

        // Choose the layout based on whether the event has an image
        if (convertView == null || (hasImage && convertView.getTag() == null) || (!hasImage && convertView.getTag() != null)) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (hasImage) {
                convertView = inflater.inflate(R.layout.history_mood_item_with_image, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.history_mood_item_without_image, parent, false);
            }
            holder = new ViewHolder();

            // Initialize views
            holder.usernameText = convertView.findViewById(R.id.text_username);
            holder.moodText = convertView.findViewById(R.id.text_mood);
            holder.reasonText = convertView.findViewById(R.id.text_reason);
            holder.socialText = convertView.findViewById(R.id.text_social_situation);
            holder.timeText = convertView.findViewById(R.id.text_time);
            holder.locationText = convertView.findViewById(R.id.text_location);
            if (hasImage) {
                holder.photo = convertView.findViewById(R.id.image_photo);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Fetch username using ProfileProvider
        ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                if (moodEvent.getUserId() != null) {
                    if (provider.getProfileByUID(moodEvent.getUserId()) != null) {
                        holder.usernameText.setText("@" + provider.getProfileByUID(moodEvent.getUserId()).getUsername());
                    } else {
                        holder.usernameText.setText("@" + moodEvent.getUserId());
                    }
                }
            }

            @Override
            public void onError(String error) {
                holder.usernameText.setText("N/A");
            }
        });

        // Mood with emoji
        String moodWithEmoji = getMoodWithEmoji(moodEvent.getEmotionalState());
        holder.moodText.setText("Mood: " + moodWithEmoji);

        // Reason
        holder.reasonText.setText("Reason: " + (moodEvent.getReason() != null ? moodEvent.getReason() : "N/A"));

        // Social Situation
        holder.socialText.setText("Social: " + (moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "N/A"));

        // Time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.timeText.setText("Time: " + sdf.format(moodEvent.getDate()));

        // Location
        double lat = moodEvent.getLatitude();
        double lng = moodEvent.getLongitude();
        if (lat == 0.0 && lng == 0.0) {
            holder.locationText.setText("Location: N/A");
        } else {
            holder.locationText.setText("Location: " + lat + ", " + lng);
        }

        // Display photo if available
        if (hasImage && holder.photo != null) {
            Glide.with(context)
                    .load(moodEvent.getPhotoUri())
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.photo);
        }

        // Click listener for editing a mood event
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMoodEvent(moodEvent, position);
            }
        });

        // Long click listener for deleting a mood event
        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMoodEvent(moodEvent, position);
            }
            return true;
        });

        return convertView;
    }

    private String getMoodWithEmoji(String mood) {
        if (mood == null) return "Unknown";
        switch (mood.toLowerCase()) {
            case "sadness":
                return "Sadness 😢";
            case "anger":
                return "Anger 😡";
            case "happiness":
                return "Happiness 😊";
            case "shame":
                return "Shame 😳";
            case "confusion":
                return "Confusion 😕";
            case "disgust":
                return "Disgust 🤢";
            case "fear":
                return "Fear 😱";
            case "surprise":
                return "Surprise 😲";
            default:
                return mood;
        }
    }
}