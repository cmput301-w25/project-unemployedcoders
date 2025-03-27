package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements ProfileEditFragment.EditProfileListener {

    UserProfile profile;
    private RecyclerView recyclerViewMoodHistory;
    private MoodEventRecyclerAdapter moodAdapter;
    private List<MoodEvent> moodEvents;

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

        ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
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

                if (FirebaseAuth.getInstance().getCurrentUser() != null && profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
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
                    followButton.setOnClickListener(v -> {
                        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String targetUserUid = profile.getUID();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Update the current user's "following" list and count
                        db.collection("users").document(currentUserUid)
                                .update(
                                        "following", FieldValue.arrayUnion(targetUserUid),
                                        "followingCount", FieldValue.increment(1)
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Successfully updated following list for user: " + currentUserUid);

                                    // Update the target user's "followers" list and count
                                    db.collection("users").document(targetUserUid)
                                            .update(
                                                    "followers", FieldValue.arrayUnion(currentUserUid),
                                                    "followersCount", FieldValue.increment(1)
                                            )
                                            .addOnSuccessListener(aVoid2 -> {
                                                Log.d("Firestore", "Successfully updated followers list for user: " + targetUserUid);
                                                Toast.makeText(ProfileActivity.this, "Now following " + profile.getUsername(), Toast.LENGTH_SHORT).show();
                                                followButton.setEnabled(false);
                                                followButton.setText("Following");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Firestore", "Error updating followers list", e);
                                                Toast.makeText(ProfileActivity.this, "Failed to follow user", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error updating following list", e);
                                    Toast.makeText(ProfileActivity.this, "Failed to follow user", Toast.LENGTH_SHORT).show();
                                });
                    });

                    logOutButton.setEnabled(false);
                    logOutButton.setVisibility(View.GONE);
                    showStatsButton.setEnabled(false);
                    showStatsButton.setVisibility(View.GONE);
                    editProfileButton.setEnabled(false);
                    editProfileButton.setVisibility(View.GONE);
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
                    return true;
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