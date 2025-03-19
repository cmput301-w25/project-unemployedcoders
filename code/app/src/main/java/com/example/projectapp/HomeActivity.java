// -----------------------------------------------------------------------------
// File: HomeActivity.java
// -----------------------------------------------------------------------------
// This file defines the HomeActivity class, which serves as the main screen in
// the ProjectApp for displaying MoodEvent lists in "For You" and "Following" tabs.
// It uses a RecyclerView to display events, includes tab navigation, and provides
// a button to add new events. The activity also features a BottomNavigationView
// for navigating between app sections. It follows the Model-View-Controller (MVC)
// pattern, acting as the controller.
//
// Design Pattern: MVC (Controller)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMoodEvents;
    private TextView tabForYou, tabFollowing;
    private MoodEventRecyclerAdapter adapter;
    private List<MoodEvent> forYouEvents = new ArrayList<>();
    private List<MoodEvent> followingEvents = new ArrayList<>();
    private Button addEventButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        recyclerViewMoodEvents = findViewById(R.id.recycler_view_mood_events);
        tabForYou = findViewById(R.id.tab_for_you);
        tabFollowing = findViewById(R.id.tab_following);
        addEventButton = findViewById(R.id.add_event_button);

        adapter = new MoodEventRecyclerAdapter(this, forYouEvents, new MoodEventRecyclerAdapter.OnFollowClickListener() {

            @Override
            public void onFollowClick(MoodEvent event) {

            }
        });

        recyclerViewMoodEvents.setAdapter(adapter);
        recyclerViewMoodEvents.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // Load public events for the "For You" tab
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

        tabForYou.performClick();

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home); // Highlight current screen
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_home) {
                return true; // Already here
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
                finish(); // Optional: close current Activity
            }
            return true;
        });


    }

    private void loadForYouEvents() {
        db.collection("moodEvents")
                .whereEqualTo("isPublic", true)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    forYouEvents.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        MoodEvent event = doc.toObject(MoodEvent.class);
                        if (event != null) {
                            forYouEvents.add(event);
                        }
                    }
                    Log.d("HomeActivity", "Loaded " + forYouEvents.size() + " public events.");
                    adapter.switchTab(0, forYouEvents);
                })
                .addOnFailureListener(e ->
                        Snackbar.make(findViewById(R.id.recycler_view_mood_events), "Error loading events: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
    }
}