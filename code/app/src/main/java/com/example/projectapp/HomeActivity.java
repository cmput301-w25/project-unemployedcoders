package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMoodEvents;
    private TextView tabForYou, tabFollowing;
    private MoodEventRecyclerAdapter adapter;
    private List<MoodEvent> forYouEvents = new ArrayList<>();
    private List<MoodEvent> followingEvents = new ArrayList<>();
    private Button addEventButton;
    private FirebaseFirestore db;
    private TextView usernameDisplay;
    private ImageButton mapToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("UID_DEBUG", "Currently signed-in user UID: " + currentUid);

        recyclerViewMoodEvents = findViewById(R.id.recycler_view_mood_events);
        tabForYou = findViewById(R.id.tab_for_you);
        tabFollowing = findViewById(R.id.tab_following);
        addEventButton = findViewById(R.id.add_event_button);
        usernameDisplay = findViewById(R.id.username_display);
        mapToggleButton = findViewById(R.id.map_toggle_button);

        // Fetch the current user's profile and set the username.
        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                if (userProfile != null && userProfile.getUsername() != null) {
                    usernameDisplay.setText("@" + userProfile.getUsername());
                } else {
                    usernameDisplay.setText("@unknown");
                    Toast.makeText(HomeActivity.this, "Failed to load username", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Exception e) {
                usernameDisplay.setText("@unknown");
                Toast.makeText(HomeActivity.this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Use the OnFollowClickListener from MoodEventRecyclerAdapter.
        adapter = new MoodEventRecyclerAdapter(this, forYouEvents, event -> followUser(event.getUserId()));
        recyclerViewMoodEvents.setAdapter(adapter);
        recyclerViewMoodEvents.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // Load public events for the "For You" tab from the users collection,
        // filtering the array in code.
        loadForYouEvents();

        tabForYou.setOnClickListener(v -> {
            Log.d("HomeActivity", "For You tab clicked. Number of events: " + forYouEvents.size());
            tabForYou.setTextColor(getResources().getColor(android.R.color.white));
            tabFollowing.setTextColor(getResources().getColor(android.R.color.darker_gray));
            adapter.switchTab(0, forYouEvents);
        });

        tabFollowing.setOnClickListener(v -> {
            tabFollowing.setTextColor(getResources().getColor(android.R.color.white));
            tabForYou.setTextColor(getResources().getColor(android.R.color.darker_gray));
            adapter.switchTab(1, followingEvents);
        });

        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodEventActivity.class);
            startActivity(intent);
        });

        // Default to "For You" tab.
        tabForYou.performClick();

        // Bottom Navigation Setup.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home); // Highlight current screen.
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_home) {
                return true; // Already on Home.
            } else if (item.getItemId() == R.id.nav_map) {
                intent = new Intent(this, MapActivity.class);
            } else if (item.getItemId() == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (item.getItemId() == R.id.nav_inbox) {
                intent = new Intent(this, InboxActivity.class);
            } else if (item.getItemId() == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
            return true;
        });
    }

    /**
     * Loads all user documents from "users", then loops through each user's
     * events array (if any), collecting only those events where public=true.
     * Finally, sorts them by date descending and updates the adapter.
     */
    private void loadForYouEvents() {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    forYouEvents.clear();
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        // no user docs
                        adapter.switchTab(0, forYouEvents);
                        return;
                    }
                    for (DocumentSnapshot userDoc : querySnapshot) {
                        UserProfile up = userDoc.toObject(UserProfile.class);
                        if (up != null && up.getHistory() != null) {
                            for (MoodEvent e : up.getHistory().getEvents()) {
                                if (e.isPublic()) {
                                    forYouEvents.add(e);
                                }
                            }
                        }
                    }
                    // sort by date descending
                    forYouEvents.sort((a,b)-> b.getDate().compareTo(a.getDate()));
                    adapter.switchTab(0, forYouEvents);
                });
    }


    private void followUser(String userId) {
        Toast.makeText(this, "Followed user: " + userId, Toast.LENGTH_SHORT).show();
    }
}
