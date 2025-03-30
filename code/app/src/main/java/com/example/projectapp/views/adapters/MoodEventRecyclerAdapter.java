package com.example.projectapp.views.adapters;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.activities.ProfileActivity;
import com.example.projectapp.views.fragments.CommentDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

/**
 * Shows a list of MoodEvent objects (public) in a RecyclerView
 */
public class MoodEventRecyclerAdapter extends RecyclerView.Adapter<MoodEventRecyclerAdapter.ViewHolder> {

    private List<MoodEvent> moodEvents;
    private final Context context;
    private final FragmentActivity activity; // Host activity reference
    private final OnFollowClickListener followListener;

    // Define view types
    private static final int VIEW_TYPE_WITH_IMAGE = 1;
    private static final int VIEW_TYPE_NO_IMAGE = 2;

    public interface OnFollowClickListener {
        void onFollowClick(MoodEvent event);
    }

    public MoodEventRecyclerAdapter(FragmentActivity activity, List<MoodEvent> moodEvents, OnFollowClickListener listener) {
        this.activity = activity;
        this.context = activity; // Activity is a Context
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

    @Override
    public int getItemViewType(int position) {
        MoodEvent event = moodEvents.get(position);
        return (event.getPhotoUri() != null && !event.getPhotoUri().toString().isEmpty())
                ? VIEW_TYPE_WITH_IMAGE
                : VIEW_TYPE_NO_IMAGE;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            itemView = LayoutInflater.from(context).inflate(R.layout.public_mood_item, parent, false);
        } else {
            itemView = LayoutInflater.from(context).inflate(R.layout.public_mood_item_no_img, parent, false);
        }
        return new ViewHolder(itemView, viewType);
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
        ImageView photoImage; // Only for VIEW_TYPE_WITH_IMAGE
        Button followButton;
        // New fields for comments
        RecyclerView commentsRecycler;
        Button addCommentButton;
        int viewType;

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            usernameText = itemView.findViewById(R.id.text_username);
            moodText = itemView.findViewById(R.id.text_mood);
            reasonText = itemView.findViewById(R.id.text_reason);
            socialText = itemView.findViewById(R.id.text_social_situation);
            timeText = itemView.findViewById(R.id.text_time);
            locationText = itemView.findViewById(R.id.text_location);
            followButton = itemView.findViewById(R.id.button_follow);
            if (viewType == VIEW_TYPE_WITH_IMAGE) {
                photoImage = itemView.findViewById(R.id.image_photo);
            }
            // Bind the nested RecyclerView and the add comment button.
            commentsRecycler = itemView.findViewById(R.id.comments_recycler);
            addCommentButton = itemView.findViewById(R.id.button_add_comment);
        }

        void bind(MoodEvent event) {
            // Bind username and mood event details (existing logic)
            ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
            provider.listenForUpdates(new ProfileProvider.DataStatus() {
                @Override
                public void onDataUpdated() {
                    UserProfile profile = provider.getProfileByUID(event.getUserId());
                    if (profile != null && profile.getUsername() != null) {
                        usernameText.setText("@" + profile.getUsername());
                        usernameText.setOnClickListener(v -> {
                            Intent intent = new Intent(itemView.getContext(), ProfileActivity.class);
                            intent.putExtra("uid", event.getUserId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            itemView.getContext().startActivity(intent);
                        });
                    } else {
                        usernameText.setText("N/A");
                    }
                }
                @Override
                public void onError(String error) {
                    // Log error if needed
                }
            });

            moodText.setText("Mood: " + (event.getEmotionalState() != null ? event.getEmotionalState() : "N/A"));
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));
            socialText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));
            if (event.getDate() != null) {
                timeText.setText("Time: " + getRelativeTime(event.getDate()));
            } else {
                timeText.setText("Time: N/A");
            }
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            if (lat == 0.0 && lng == 0.0) {
                locationText.setText("Location: N/A");
            } else {
                locationText.setText("Location: " + lat + ", " + lng);
            }
            if (viewType == VIEW_TYPE_WITH_IMAGE && photoImage != null) {
                Glide.with(itemView.getContext())
                        .load(event.getPhotoUri())
                        .placeholder(android.R.color.darker_gray)
                        .into(photoImage);
            }

            // Follow button logic (existing)
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getUid().equals(event.getUserId())) {
                    followButton.setVisibility(View.GONE);
                } else {
                    UserProfile currentUserProfile = ProfileProvider.getInstance(FirebaseFirestore.getInstance())
                            .getProfileByUID(currentUser.getUid());
                    if (currentUserProfile != null && currentUserProfile.getFollowing().contains(event.getUserId())) {
                        followButton.setText("Already Following");
                        followButton.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        followButton.setEnabled(false);
                        followButton.setVisibility(View.VISIBLE);
                    } else {
                        followButton.setText("Follow");
                        followButton.setEnabled(true);
                        followButton.setVisibility(View.VISIBLE);
                        followButton.setOnClickListener(v -> {
                            if (followListener != null) {
                                followListener.onFollowClick(event);
                            }
                        });
                    }
                }
            }

            // ***** Comments Integration *****
            if (event.getComments() != null && !event.getComments().isEmpty()) {
                commentsRecycler.setVisibility(View.VISIBLE);
                CommentAdapter commentAdapter = new CommentAdapter(context, event.getComments());
                commentsRecycler.setAdapter(commentAdapter);
                commentsRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
            } else {
                commentsRecycler.setVisibility(View.GONE);
            }
            addCommentButton.setOnClickListener(v -> {
                CommentDialogFragment.newInstance(event)
                        .show(activity.getSupportFragmentManager(), "AddCommentDialog");
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