package com.example.projectapp.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectapp.R;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.models.MoodEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MoodHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_WITH_IMAGE = 1;
    private static final int VIEW_TYPE_WITHOUT_IMAGE = 2;

    private ArrayList<MoodEvent> moodEvents;
    private Context context;
    private OnMoodEventClickListener listener;

    // Interface for click events
    public interface OnMoodEventClickListener {
        void onEditMoodEvent(MoodEvent event, int position);
        void onDeleteMoodEvent(MoodEvent event, int position);
    }

    public MoodHistoryAdapter(Context context, ArrayList<MoodEvent> moodEvents, OnMoodEventClickListener listener) {
        this.context = context;
        this.moodEvents = moodEvents;
        this.listener = listener;
    }

    // Update the adapter's data and refresh the list
    public void setData(ArrayList<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MoodEvent event = moodEvents.get(position);
        return (event.getPhotoUri() != null && !event.getPhotoUri().toString().isEmpty())
                ? VIEW_TYPE_WITH_IMAGE
                : VIEW_TYPE_WITHOUT_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            view = LayoutInflater.from(context).inflate(R.layout.history_mood_item_with_image, parent, false);
            return new MoodEventWithImageViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.history_mood_item_without_image, parent, false);
            return new MoodEventWithoutImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        if (holder instanceof MoodEventWithImageViewHolder) {
            ((MoodEventWithImageViewHolder) holder).bind(event);
        } else {
            ((MoodEventWithoutImageViewHolder) holder).bind(event);
        }

        // Set click listeners for editing and deleting
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMoodEvent(event, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMoodEvent(event, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return moodEvents != null ? moodEvents.size() : 0;
    }

    // ViewHolder for items with an image
    static class MoodEventWithImageViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, moodText, reasonText, socialText, timeText, locationText;
        ImageView photoImage;

        MoodEventWithImageViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            moodText = itemView.findViewById(R.id.text_mood);
            reasonText = itemView.findViewById(R.id.text_reason);
            socialText = itemView.findViewById(R.id.text_social_situation);
            timeText = itemView.findViewById(R.id.text_time);
            locationText = itemView.findViewById(R.id.text_location);
            photoImage = itemView.findViewById(R.id.image_photo);
        }

        void bind(MoodEvent event) {
            // Fetch username using ProfileProvider
            ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
            provider.listenForUpdates(new ProfileProvider.DataStatus() {
                @Override
                public void onDataUpdated() {
                    if (event.getUserId() != null) {
                        if (provider.getProfileByUID(event.getUserId()) != null) {
                            usernameText.setText("@" + provider.getProfileByUID(event.getUserId()).getUsername());
                        } else {
                            usernameText.setText("@" + event.getUserId());
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    usernameText.setText("N/A");
                }
            });

            // Mood with emoji
            String moodWithEmoji = (event.getEmotionalState());
            moodText.setText("Mood: " + moodWithEmoji);

            // Reason
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));

            // Social Situation
            socialText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));

            // Time
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            timeText.setText("Time: " + sdf.format(event.getDate()));

            // Location
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            if (lat == 0.0 && lng == 0.0) {
                locationText.setText("Location: N/A");
            } else {
                locationText.setText("Location: " + lat + ", " + lng);
            }

            // Load the image
            if (event.getPhotoUri() != null) {
                Glide.with(itemView.getContext())
                        .load(event.getPhotoUri())
                        .placeholder(android.R.color.darker_gray)
                        .into(photoImage);
            }
        }

    }

    // ViewHolder for items without an image
    static class MoodEventWithoutImageViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, moodText, reasonText, socialText, timeText, locationText;

        MoodEventWithoutImageViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            moodText = itemView.findViewById(R.id.text_mood);
            reasonText = itemView.findViewById(R.id.text_reason);
            socialText = itemView.findViewById(R.id.text_social_situation);
            timeText = itemView.findViewById(R.id.text_time);
            locationText = itemView.findViewById(R.id.text_location);
        }

        void bind(MoodEvent event) {
            // Fetch username using ProfileProvider
            ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
            provider.listenForUpdates(new ProfileProvider.DataStatus() {
                @Override
                public void onDataUpdated() {
                    if (event.getUserId() != null) {
                        if (provider.getProfileByUID(event.getUserId()) != null) {
                            usernameText.setText("@" + provider.getProfileByUID(event.getUserId()).getUsername());
                        } else {
                            usernameText.setText("@" + event.getUserId());
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    usernameText.setText("N/A");
                }
            });

            // Mood with emoji
            String moodWithEmoji = (event.getEmotionalState());
            moodText.setText("Mood: " + moodWithEmoji);

            // Reason
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));

            // Social Situation
            socialText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));

            // Time
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            timeText.setText("Time: " + sdf.format(event.getDate()));

            // Location
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            if (lat == 0.0 && lng == 0.0) {
                locationText.setText("Location: N/A");
            } else {
                locationText.setText("Location: " + lat + ", " + lng);
            }
        }
    }
}