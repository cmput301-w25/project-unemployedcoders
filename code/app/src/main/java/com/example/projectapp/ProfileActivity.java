// -----------------------------------------------------------------------------
// File: ProfileActivity.java
// -----------------------------------------------------------------------------
// This file defines the ProfileActivity class, which serves as the user profile
// screen in the ProjectApp. It sets up a BottomNavigationView for navigating
// between app sections and highlights the "Profile" tab. The activity follows
// the Model-View-Controller (MVC) pattern, acting as the controller.
//
// Design Pattern: MVC (Controller)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements ProfileEditFragment.EditProfileListener {

    UserProfile profile;
    private RecyclerView recyclerViewMoodHistory;
    private MoodEventRecyclerAdapter moodAdapter;
    private List<MoodEvent> moodEvents;
    private Button followButton;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewMoodHistory = findViewById(R.id.recycler_view_mood_history);
        moodEvents = new ArrayList<>();
        moodAdapter = new MoodEventRecyclerAdapter(this, moodEvents, event -> followUser(event.getUserId()));
        recyclerViewMoodHistory.setAdapter(moodAdapter);
        recyclerViewMoodHistory.setLayoutManager(new LinearLayoutManager(this));
        followButton = findViewById(R.id.follow_profile_button);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ProfileProvider provider = ProfileProvider.getInstance(db);
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                /*
                IMPORTANT: We can't do anything until firebase actually gives the profiles back
                 */

                Bundle info = getIntent().getExtras();
                if (info != null && info.getString("uid") != null) {
                    profile = provider.getProfileByUID(info.getString("uid"));
                    Log.d("Testing", "Option 1");
                } else {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        profile = provider.getProfileByUID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Log.d("Testing", "Option 2");
                    }
                }

                showUsername();
                showRecentEvents();
                if (profile == null) {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                showUserDetails();

                Button logOutButton = findViewById(R.id.logout_button);
                Button showStatsButton = findViewById(R.id.button_show_stats);
                Button editProfileButton = findViewById(R.id.edit_profile_button);
                Button followButton = findViewById(R.id.follow_profile_button);

                if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                        profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    logOutButton.setOnClickListener(v -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });

                    showStatsButton.setOnClickListener(v -> {
                        Intent statsIntent = new Intent(ProfileActivity.this, StatsActivity.class);
                        startActivity(statsIntent); // Fixed: Changed 'intent' to 'statsIntent'
                    });

                    editProfileButton.setOnClickListener(v -> {
                        ProfileEditFragment.newInstance(profile)
                                .show(getSupportFragmentManager(), "Edit Profile");
                    });

                    followButton.setEnabled(false);
                    followButton.setVisibility(View.GONE);
                } else {
                    logOutButton.setEnabled(false);
                    logOutButton.setVisibility(View.GONE);
                    showStatsButton.setEnabled(false);
                    showStatsButton.setVisibility(View.GONE);
                    editProfileButton.setEnabled(false);
                    editProfileButton.setVisibility(View.GONE);

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        UserProfile currentUserProfile = provider.getProfileByUID(auth.getCurrentUser().getUid());
                        if (currentUserProfile != null &&
                                currentUserProfile.getFollowing().contains(profile.getUID())) {
                            followButton.setText("Unfollow");
                            followButton.setTextColor(ContextCompat.getColor(ProfileActivity.this, android.R.color.white));
                            followButton.setEnabled(true);
                            followButton.setVisibility(View.VISIBLE);
                            followButton.setOnClickListener(v -> {
                                unfollowUser(profile.getUID());
                            });
                        } else {
                            followButton.setText("Follow");
                            followButton.setEnabled(true);
                            followButton.setVisibility(View.VISIBLE);
                            followButton.setTextColor(ContextCompat.getColor(ProfileActivity.this, android.R.color.white));
                            followButton.setOnClickListener(v -> {
                                sendFollowRequest(profile.getUID());
                            });
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Error loading profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
            bottomNav.setOnItemSelectedListener(item -> {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(this, ProfileActivity.class);
                } else if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(this, HomeActivity.class);
                } else if (item.getItemId() == R.id.nav_map) {
                    intent = new Intent(this, MapActivity.class);
                } else if (item.getItemId() == R.id.nav_history) {
                    intent = new Intent(this, HistoryActivity.class);
                } else if (item.getItemId() == R.id.nav_inbox) {
                    intent = new Intent(this, InboxActivity.class);
                }

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                return true;
            });
        } else {
            Log.e("ProfileActivity", "BottomNavigationView not found");
            Log.e("ProfileActivity", "BottomNavigationView not found");
        }
    }

    private void showUserDetails() {
        TextView usernameText = findViewById(R.id.profile_username);
        TextView nameText = findViewById(R.id.profile_name);
        usernameText.setText("@" + profile.getUsername());
        nameText.setText("Name: " + profile.getName());

        // Display mood history (only public events if not the current user)
        moodEvents.clear();
        if (profile.getHistory() != null) {
            boolean isCurrentUser = FirebaseAuth.getInstance().getCurrentUser() != null &&
                    profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for (MoodEvent event : profile.getHistory().getEvents()) {
                if (isCurrentUser || event.isPublic()) {
                    moodEvents.add(event);
                }
            }
            moodEvents.sort((a, b) -> b.getDate().compareTo(a.getDate())); // Sort by date descending
            moodAdapter.switchTab(0, moodEvents);
        }
    }

    private void showUsername() {
        TextView usernameText = findViewById(R.id.profile_username);
        if (profile != null && profile.getUsername() != null) {
            usernameText.setText(profile.getUsername());
        } else {
            usernameText.setText("N/A");
        }
    }

    private void showRecentEvents() {
        LinearLayout moodHistoryContainer = findViewById(R.id.mood_history_container);
        if (moodHistoryContainer == null) {
            Log.e("ProfileActivity", "LinearLayout with ID mood_history_container not found in layout");
            return;
        }

        if (profile == null) {
            Log.e("ProfileActivity", "Profile is null in showRecentEvents");
            return;
        }

        // Clear any existing views
        moodHistoryContainer.removeAllViews();

        // Get the 3 most recent events
        List<MoodEvent> recentEvents = profile.getRecentEvents();
        boolean isOwnProfile = FirebaseAuth.getInstance().getCurrentUser() != null &&
                profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Inflate the appropriate layout for each event
        LayoutInflater inflater = LayoutInflater.from(this);
        for (MoodEvent event : recentEvents) {
            // Determine which layout to use based on whether the event has a photo
            boolean hasPhoto = event.getPhotoUriRaw() != null && !event.getPhotoUriRaw().isEmpty();
            View itemView = inflater.inflate(
                    hasPhoto ? R.layout.public_mood_item : R.layout.public_mood_item_no_img,
                    moodHistoryContainer,
                    false
            );

            // Populate the views
            TextView usernameText = itemView.findViewById(R.id.text_username);
            TextView moodText = itemView.findViewById(R.id.text_mood);
            TextView reasonText = itemView.findViewById(R.id.text_reason);
            TextView socialSituationText = itemView.findViewById(R.id.text_social_situation);
            ImageView photoImage = hasPhoto ? itemView.findViewById(R.id.image_photo) : null;
            TextView timeText = itemView.findViewById(R.id.text_time);
            TextView locationText = itemView.findViewById(R.id.text_location);
            Button followButton = itemView.findViewById(R.id.button_follow);

            usernameText.setText(profile.getUsername() != null ? profile.getUsername() : "N/A");
            moodText.setText("Mood: " + event.getEmotionalState());
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));
            socialSituationText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));
            timeText.setText("Time: " + new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(event.getDate()));

            if (event.getLatitude() != 0.0 || event.getLongitude() != 0.0) {
                locationText.setText(String.format(Locale.getDefault(), "Location: (%.4f, %.4f)", event.getLatitude(), event.getLongitude()));
            } else {
                locationText.setText("Location: N/A");
            }

            // Load the photo if the layout includes an ImageView
            if (hasPhoto && photoImage != null) {
                Picasso.get().load(event.getPhotoUriRaw()).into(photoImage);
            }

            // Handle the follow button visibility
            if (isOwnProfile) {
                followButton.setVisibility(View.GONE);
            } else {
                followButton.setVisibility(View.VISIBLE);
                followButton.setOnClickListener(v -> {
                    sendFollowRequest(profile.getUID());
                });
            }

            // Add the inflated view to the container
            moodHistoryContainer.addView(itemView);
        }
    }

    private void sendFollowRequest(String targetUserId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(ProfileActivity.this, "No logged in user.", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUid = auth.getCurrentUser().getUid();
        if (currentUid.equals(targetUserId)) {
            Toast.makeText(ProfileActivity.this, "You cannot follow yourself.", Toast.LENGTH_SHORT).show();
            return;
        }
        FollowRequest request = new FollowRequest(currentUid, currentUid, "pending");

        FirebaseFirestore.getInstance().collection("users")
                .document(targetUserId)
                .collection("requests")
                .document(currentUid)
                .set(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Follow request sent!", Toast.LENGTH_SHORT).show();
                    followButton.setText("Following");
                    followButton.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to send follow request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser(String targetUserId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(ProfileActivity.this, "No logged in user.", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUid = auth.getCurrentUser().getUid();
        ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
        UserProfile currentUserProfile = provider.getProfileByUID(currentUid);
        if (currentUserProfile != null && currentUserProfile.getFollowing().contains(targetUserId)) {
            currentUserProfile.getFollowing().remove(targetUserId);
            FirebaseFirestore.getInstance().collection("users")
                    .document(currentUid)
                    .set(currentUserProfile)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Unfollowed successfully", Toast.LENGTH_SHORT).show();
                        followButton.setText("Follow");
                        followButton.setOnClickListener(v -> {
                            sendFollowRequest(targetUserId);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    private void followUser(String userId) {
        Toast.makeText(this, "Followed user: " + userId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProfileEdited(UserProfile profile) {
        String uid = profile.getUID();
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "User profile saved! events=" + profile.getHistory().getEvents().size()))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error saving user profile", e));
        this.profile = profile; // Update local profile
        showUserDetails(); // Refresh UI
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }
}