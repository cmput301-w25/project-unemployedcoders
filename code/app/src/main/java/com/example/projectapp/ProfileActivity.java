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
    private Button followButton, backToSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        // Enable ActionBar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        recyclerViewMoodHistory = findViewById(R.id.recycler_view_mood_history);
        followButton = findViewById(R.id.follow_profile_button);
        backToSearchButton = findViewById(R.id.back_to_search_button);
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
                } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    profile = provider.getProfileByUID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }

                if (profile == null) {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                showUserDetails();
                setupFollowButton();

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
        }
    }

    private void showUserDetails() {
        TextView usernameText = findViewById(R.id.profile_username);
        TextView nameText = findViewById(R.id.profile_name);
        TextView followingCount = findViewById(R.id.following_count);
        TextView followersCount = findViewById(R.id.followers_count);
        usernameText.setText("@" + profile.getUsername());
        nameText.setText("Name: " + profile.getName());
        followingCount.setText("Following: " + profile.getFollowingCount());
        followersCount.setText("Followers: " + profile.getFollowersCount());

        moodEvents.clear();
        if (profile.getHistory() != null) {
            boolean isCurrentUser = FirebaseAuth.getInstance().getCurrentUser() != null &&
                    profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for (MoodEvent event : profile.getHistory().getEvents()) {
                if (isCurrentUser || event.isPublic()) {
                    moodEvents.add(event);
                }
            }
            moodEvents.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            moodAdapter.switchTab(0, moodEvents);
        }
    }

    private void setupFollowButton() {
        Button logOutButton = findViewById(R.id.logout_button);
        Button showStatsButton = findViewById(R.id.button_show_stats);
        Button editProfileButton = findViewById(R.id.edit_profile_button);

        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            logOutButton.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            });

            showStatsButton.setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));

            editProfileButton.setOnClickListener(v ->
                    ProfileEditFragment.newInstance(profile).show(getSupportFragmentManager(), "Edit Profile"));

            followButton.setEnabled(false);
            followButton.setVisibility(View.GONE);
        } else {
            logOutButton.setEnabled(false);
            logOutButton.setVisibility(View.GONE);
            showStatsButton.setEnabled(false);
            showStatsButton.setVisibility(View.GONE);
            editProfileButton.setEnabled(false);
            editProfileButton.setVisibility(View.GONE);

            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            UserProfile currentUserProfile = ProfileProvider.getInstance(FirebaseFirestore.getInstance())
                    .getProfileByUID(currentUserUid);

            if (currentUserProfile != null && currentUserProfile.getFollowing().contains(profile.getUID())) {
                followButton.setText("Unfollow");
                followButton.setOnClickListener(v -> unfollowUser());
            } else {
                followButton.setText("Follow");
                followButton.setOnClickListener(v -> followUser());
            }
        }
    }

    private void followUser() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String targetUserUid = profile.getUID();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserUid)
                .update(
                        "following", FieldValue.arrayUnion(targetUserUid),
                        "followingCount", FieldValue.increment(1)
                )
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUserUid)
                            .update(
                                    "followers", FieldValue.arrayUnion(currentUserUid),
                                    "followersCount", FieldValue.increment(1)
                            )
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Now following " + profile.getUsername(), Toast.LENGTH_SHORT).show();
                                followButton.setText("Unfollow");
                                followButton.setOnClickListener(v -> unfollowUser());
                                showUserDetails();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to follow: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to follow: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void unfollowUser() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String targetUserUid = profile.getUID();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserUid)
                .update(
                        "following", FieldValue.arrayRemove(targetUserUid),
                        "followingCount", FieldValue.increment(-1)
                )
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUserUid)
                            .update(
                                    "followers", FieldValue.arrayRemove(currentUserUid),
                                    "followersCount", FieldValue.increment(-1)
                            )
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Unfollowed " + profile.getUsername(), Toast.LENGTH_SHORT).show();
                                followButton.setText("Follow");
                                followButton.setOnClickListener(v -> followUser());
                                showUserDetails();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to unfollow: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void followUser(String userId) {
        Toast.makeText(this, "Followed user: " + userId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProfileEdited(UserProfile profile) {
        FirebaseFirestore.getInstance().collection("users")
                .document(profile.getUID())
                .set(profile)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User profile saved!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user profile", e));
        this.profile = profile;
        showUserDetails();
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
}