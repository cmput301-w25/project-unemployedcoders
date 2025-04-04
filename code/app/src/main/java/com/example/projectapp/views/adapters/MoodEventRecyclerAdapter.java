package com.example.projectapp.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.Comment;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.activities.ProfileActivity;
import com.example.projectapp.views.fragments.CommentDialogFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Shows a list of MoodEvent objects (public) in a RecyclerView
 */
public class MoodEventRecyclerAdapter extends RecyclerView.Adapter<MoodEventRecyclerAdapter.ViewHolder> implements CommentDialogFragment.CommentDialogListener {

    private List<MoodEvent> moodEvents;
    private final Context context;
    private final OnFollowClickListener followListener;

    // Define view types
    private static final int VIEW_TYPE_WITH_IMAGE = 1;
    private static final int VIEW_TYPE_NO_IMAGE = 2;

    @Override
    public void onCommentAdded(Comment comment, MoodEvent moodEvent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("users").document(moodEvent.getUserId());
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserProfile profile = documentSnapshot.toObject(UserProfile.class);

                if (profile != null && profile.getHistory() != null){

                    int index = profile.getHistory().getEvents().indexOf(moodEvent);
                    if (index >= 0){
                        MoodEvent oldEvent = profile.getHistory().getEvents().get(index);
                        if (oldEvent.getComments() == null){
                            oldEvent.setComments(new ArrayList<>());
                        }
                        oldEvent.addComment(comment);

                        ref.set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                int index = moodEvents.indexOf(moodEvent);
                                if (index != -1) {
                                    notifyItemChanged(index);
                                }
                            }
                        });

                    } else {
                        Log.d("ATest", "Index is -1");
                    }
                } else {
                    Log.d("ATest", "Profile or history is null");
                }
            }
        });

    }

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

    @Override
    public int getItemViewType(int position) {
        MoodEvent event = moodEvents.get(position);
        // Check if the event has a photo URI
        return (event.getPhotoUri() != null && !event.getPhotoUri().toString().isEmpty())
                ? VIEW_TYPE_WITH_IMAGE
                : VIEW_TYPE_NO_IMAGE;
    }

    @NonNull
    @Override
    public MoodEventRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the appropriate layout based on the view type
        View itemView;
        itemView = LayoutInflater.from(context).inflate(R.layout.public_mood_item, parent, false);
        return new ViewHolder(itemView, viewType);
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
        TextView usernameText, moodText, reasonText, socialText, timeText, locationText;
        ImageView photoImage; // Will be null for VIEW_TYPE_NO_IMAGE
        Button followButton, viewCommentButton, addCommentButton;
        ListView commentList;
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
            photoImage = itemView.findViewById(R.id.image_photo);
            addCommentButton = itemView.findViewById(R.id.add_comment_button);
        }

        void bind(MoodEvent event) {
            // Fetch username using ProfileProvider and set it on usernameText
            ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
            provider.listenForUpdates(new ProfileProvider.DataStatus() {
                @Override
                public void onDataUpdated() {
                    UserProfile profile = provider.getProfileByUID(event.getUserId());
                    if (profile != null && profile.getUsername() != null) {
                        usernameText.setText("@" + profile.getUsername());
                        // When clicking the username, open ProfileActivity
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
                    Log.e("DB Error", "Error getting username: " + error);
                    usernameText.setText("@N/A");
                }
            });

            // Set mood details
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

            /*
            https://bumptech.github.io/glide/
            https://www.geeksforgeeks.org/image-loading-caching-library-android-set-2/ (Shubham)
             */

            // Load the image only if the view type includes an ImageView
            if (viewType == VIEW_TYPE_WITH_IMAGE && photoImage != null) {
                Glide.with(itemView.getContext())
                        .load(event.getPhotoUri())
                        .placeholder(android.R.color.darker_gray)
                        .into(photoImage);
            } else {
                photoImage.setVisibility(View.GONE);
            }


            addCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentActivity activity = (FragmentActivity) itemView.getContext();
                    CommentDialogFragment dialog = CommentDialogFragment.newInstance(event);
                    dialog.show(activity.getSupportFragmentManager(), "Add Comment");
                }
            });



            // Manage follow button behavior
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Hide follow button if the event belongs to the current user
                if (currentUser.getUid().equals(event.getUserId())) {
                    followButton.setVisibility(View.GONE);
                } else {
                    // Check if the current user is already following this event's user
                    UserProfile currentUserProfile = ProfileProvider.getInstance(FirebaseFirestore.getInstance())
                            .getProfileByUID(currentUser.getUid());
                    if (currentUserProfile != null && currentUserProfile.getFollowing().contains(event.getUserId())) {
                        // Already following: show "Following" text and disable clicks
                        followButton.setText("Already Following");
                        followButton.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        followButton.setEnabled(false);
                        followButton.setVisibility(View.VISIBLE);
                    } else {
                        // Not following yet: show "Follow" and attach click listener
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
        }

        private String getRelativeTime(Date date) {
            long diffMillis = System.currentTimeMillis() - date.getTime();
            long diffMinutes = diffMillis / 60000;
            if (diffMinutes < 1) return "Just now";
            else if (diffMinutes < 60) return diffMinutes + " minutes ago";
            else return date.toString();
        }
    }}

