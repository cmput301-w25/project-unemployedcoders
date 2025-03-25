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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.projectapp.HistoryActivity;
import com.example.projectapp.HomeActivity;
import com.example.projectapp.InboxActivity;
import com.example.projectapp.MapActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity implements ProfileEditFragment.EditProfileListener {

    UserProfile profile;
    Button followButton; // Declare as a field so it can be updated later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile); // Must match the layout file

        // Initialize followButton from layout.
        followButton = findViewById(R.id.follow_profile_button);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ProfileProvider provider = ProfileProvider.getInstance(db);
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                // Determine which profile to display.
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

                Button logOutButton = findViewById(R.id.logout_button);
                Button showStatsButton = findViewById(R.id.button_show_stats);
                Button editProfileButton = findViewById(R.id.edit_profile_button);

                // If the profile is the current user's own profile, hide follow button.
                if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                        profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    logOutButton.setOnClickListener(v -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    });

                    showStatsButton.setOnClickListener(v -> {
                        Intent statsIntent = new Intent(ProfileActivity.this, StatsActivity.class);
                        startActivity(statsIntent);
                    });

                    editProfileButton.setOnClickListener(v -> {
                        ProfileEditFragment.newInstance(profile)
                                .show(getSupportFragmentManager(), "Edit Profile");
                    });

                    followButton.setEnabled(false);
                    followButton.setVisibility(View.GONE);
                } else {
                    // Viewing someone else's profile.
                    // Hide options for self.
                    logOutButton.setEnabled(false);
                    logOutButton.setVisibility(View.GONE);
                    showStatsButton.setEnabled(false);
                    showStatsButton.setVisibility(View.GONE);
                    editProfileButton.setEnabled(false);
                    editProfileButton.setVisibility(View.GONE);

                    // Set up follow button.
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        // Get current user's profile to check following list.
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
                // Optionally handle error here.
            }
        });

        // Bottom Navigation Setup.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile); // Highlight "Profile"
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
        }
    }

    private void showUsername(){
        TextView username_text = findViewById(R.id.profile_username);
        if (profile != null && profile.getUsername() != null) {
            username_text.setText(profile.getUsername());
        } else {
            username_text.setText("N/A");
        }
    }

    /**
     * Sends a follow request to the specified target user.
     * The follow request is written to the target user's "requests" subcollection.
     *
     * @param targetUserId The UID of the user to follow.
     */
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
        // Create a new FollowRequest object using the current user's UID for both fields.
        FollowRequest request = new FollowRequest(currentUid, currentUid, "pending");

        FirebaseFirestore.getInstance().collection("users")
                .document(targetUserId)
                .collection("requests")
                .document(currentUid)
                .set(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Follow request sent!", Toast.LENGTH_SHORT).show();
                    // Update the button state to reflect that the request was sent.
                    followButton.setText("Following");
                    followButton.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to send follow request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Unfollows the target user by removing their UID from the current user's "following" array.
     *
     * @param targetUserId The UID of the user to unfollow.
     */
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
                        // Update button state to allow sending a follow request again.
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
    }
}
