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
package com.example.projectapp.views.activities;

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
import androidx.fragment.app.FragmentActivity;

import com.example.projectapp.views.fragments.FollowListDialogFragment;
import com.example.projectapp.models.Comment;
import com.example.projectapp.views.adapters.CommentAdapter;
import com.example.projectapp.views.fragments.CommentDialogFragment;
import com.example.projectapp.views.fragments.ProfileEditFragment;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.FollowRequest;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements ProfileEditFragment.EditProfileListener, CommentDialogFragment.CommentDialogListener {

    UserProfile profile;
    private Button followButton, backToSearchButton;
    private List<MoodEvent> recentEvents = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        // Enable ActionBar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        followButton = findViewById(R.id.follow_profile_button);
        backToSearchButton = findViewById(R.id.back_to_search_button);

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


                ArrayList<UserProfile> followers = new ArrayList<>();
                int followerCount = 0;
                int followingCount = 0;
                if (profile != null && provider.getProfiles() != null){
                    for (UserProfile p: provider.getProfiles()){
                        if (p.getFollowing().contains(profile.getUID())){
                            followerCount++;
                            followers.add(p);
                        }
                    }

                    followingCount = profile.getFollowing().size();
                }

                TextView followerText = findViewById(R.id.followers_count);
                TextView followingText = findViewById(R.id.following_count);

                followerText.setText("Followers: " + followerCount);
                followingText.setText("Following: " + followingCount);


                followerText.setOnClickListener(v -> {
                    FollowListDialogFragment.newInstance("followers", followers)
                            .show(getSupportFragmentManager(), "FollowListDialog");
                });

                followingText.setOnClickListener(v -> {
                    ArrayList<UserProfile> followingList = new ArrayList<>();
                    for (UserProfile p : provider.getProfiles()) {
                        if (profile.getFollowing().contains(p.getUID())) {
                            followingList.add(p);
                        }
                    }
                    FollowListDialogFragment.newInstance("following", followingList)
                            .show(getSupportFragmentManager(), "FollowListDialog");
                });

                showUsername();
                showRecentEvents();

                // Show "Back to Search" button if coming from SearchActivity
                boolean fromSearch = getIntent().getBooleanExtra("fromSearch", false);
                if (fromSearch) {
                    backToSearchButton.setVisibility(View.VISIBLE);
                    backToSearchButton.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    backToSearchButton.setVisibility(View.GONE);
                }

                if (profile == null) {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

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
        recentEvents = profile.getRecentEvents();
        boolean isOwnProfile = FirebaseAuth.getInstance().getCurrentUser() != null &&
                profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Inflate the appropriate layout for each event
        LayoutInflater inflater = LayoutInflater.from(this);
        for (MoodEvent event : recentEvents) {
            // Determine which layout to use based on whether the event has a photo
            boolean hasPhoto = event.getPhotoUriRaw() != null && !event.getPhotoUriRaw().isEmpty();
            View itemView = inflater.inflate(
                    R.layout.public_mood_item,
                    moodHistoryContainer,
                    false
            );

            // Populate the views
            TextView usernameText = itemView.findViewById(R.id.text_username);
            TextView moodText = itemView.findViewById(R.id.text_mood);
            TextView reasonText = itemView.findViewById(R.id.text_reason);
            TextView socialSituationText = itemView.findViewById(R.id.text_social_situation);
            ImageView photoImage = itemView.findViewById(R.id.image_photo);

            TextView timeText = itemView.findViewById(R.id.text_time);
            TextView locationText = itemView.findViewById(R.id.text_location);
            Button followButton = itemView.findViewById(R.id.button_follow);
            Button addCommentButton = itemView.findViewById(R.id.add_comment_button);

            usernameText.setText(profile.getUsername() != null ? profile.getUsername() : "N/A");
            moodText.setText("Mood: " + event.getEmotionalState());
            reasonText.setText("Reason: " + (event.getReason() != null ? event.getReason() : "N/A"));
            socialSituationText.setText("Social: " + (event.getSocialSituation() != null ? event.getSocialSituation() : "N/A"));
            timeText.setText("Time: " + new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(event.getDate()));


            addCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentActivity activity = (FragmentActivity) itemView.getContext();
                    CommentDialogFragment dialog = CommentDialogFragment.newInstance(event);
                    dialog.show(activity.getSupportFragmentManager(), "Add Comment");
                }
            });

            if (event.getLatitude() != 0.0 || event.getLongitude() != 0.0) {
                locationText.setText(String.format(Locale.getDefault(), "Location: (%.4f, %.4f)", event.getLatitude(), event.getLongitude()));
            } else {
                locationText.setText("Location: N/A");
            }

            // Load the photo if the layout includes an ImageView
            if (hasPhoto && photoImage != null) {
                Picasso.get().load(event.getPhotoUriRaw()).into(photoImage);
            } else {
                photoImage.setVisibility(View.GONE);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent;
        if (getIntent().getBooleanExtra("fromSearch", false)) {
            intent = new Intent(this, SearchActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

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
}