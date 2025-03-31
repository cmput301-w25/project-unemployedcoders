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

package com.example.projectapp.views.activities;

import static androidx.test.InstrumentationRegistry.getContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectapp.database_util.FirebaseSync;
import com.example.projectapp.models.Comment;
import com.example.projectapp.views.adapters.MoodEventRecyclerAdapter;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.database_util.UserProfileCallback;
import com.example.projectapp.models.FollowRequest;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.fragments.CommentDialogFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements CommentDialogFragment.CommentDialogListener {

    private RecyclerView recyclerViewMoodEvents;
    private TextView tabForYou, tabFollowing;
    private MoodEventRecyclerAdapter adapter;
    private List<MoodEvent> forYouEvents = new ArrayList<>();
    private List<MoodEvent> followingEvents = new ArrayList<>();
    private List<MoodEvent> displayEvents = new ArrayList<>();
    private Button addEventButton;
    private Button searchButton;
    private FirebaseFirestore db;
    private TextView usernameDisplay;
    private ImageButton mapToggleButton;

    private static final String TAG = "HomeActivity";

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
        searchButton = findViewById(R.id.search_button);

        // Log if searchButton is null
        if (searchButton == null) {
            Log.e(TAG, "Search button not found in layout");
        } else {
            Log.d(TAG, "Search button found, setting click listener");
            searchButton.setOnClickListener(v -> {
                Log.d(TAG, "Search button clicked, launching SearchActivity");
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching SearchActivity", e);
                    Toast.makeText(HomeActivity.this, "Failed to launch SearchActivity: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

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
        setUpdateListener();

        tabForYou.setOnClickListener(v -> {
            Log.d("HomeActivity", "For You tab clicked. Number of events: " + forYouEvents.size());
            tabForYou.setTextColor(getResources().getColor(android.R.color.white));
            tabFollowing.setTextColor(getResources().getColor(android.R.color.darker_gray));
            displayEvents = forYouEvents;
            adapter.switchTab(0, displayEvents);
        });

        tabFollowing.setOnClickListener(v -> {
            tabFollowing.setTextColor(getResources().getColor(android.R.color.white));
            tabForYou.setTextColor(getResources().getColor(android.R.color.darker_gray));
            displayEvents = followingEvents;
            adapter.switchTab(1, displayEvents);
        });

        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodEventActivity.class);
            startActivity(intent);
        });

        // Default to "For You" tab.
        tabForYou.performClick();
        displayEvents = forYouEvents;

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

    /*helper to detect if user is connected to the internet*/
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
    private void syncPendingMoodEventsIfOnline() {
        if (!isNetworkAvailable()) return;

        SharedPreferences prefs = getSharedPreferences("PendingMoodEvents", MODE_PRIVATE);
        String json = prefs.getString("pendingMoodEvents", null);
        if (json == null) return;

        // Just flag that these need syncing — HistoryActivity will handle upload
        SharedPreferences state = getSharedPreferences("OfflineSyncPrefs", MODE_PRIVATE);
        state.edit().putBoolean("offlineEventsSynced", true).apply();

        Toast.makeText(HomeActivity.this, "Offline additions ready to sync", Toast.LENGTH_SHORT).show();
    }

    private void setUpdateListener(){
        ProfileProvider provider = ProfileProvider.getInstance(db);
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                loadForYouEvents();
                loadFollowingEvents();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(String error) {
                Log.e("Error", "Error in Home Page: " + error);
            }
        });
    }

    /**
     * Loads all user documents from "users", then loops through each user's
     * events array, collecting only those events where public=true.
     * Finally, sorts them by date descending and updates the adapter.
     */
    private void loadForYouEvents() {

        forYouEvents.clear();
        ProfileProvider provider = ProfileProvider.getInstance(db);
        ArrayList<UserProfile> profiles = provider.getProfiles();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            UserProfile currentUser = provider.getProfileByUID(mAuth.getCurrentUser().getUid());
            for (UserProfile p: profiles){
                for (MoodEvent event: p.getHistory().getEvents()){
                    if (event.isPublic()){
                        forYouEvents.add(event);
                    }
                }

            }
        }

        forYouEvents.sort((a,b)-> b.getDate().compareTo(a.getDate()));

    }


    private void loadFollowingEvents(){
        followingEvents.clear();
        ProfileProvider provider = ProfileProvider.getInstance(db);
        ArrayList<UserProfile> profiles = provider.getProfiles();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            UserProfile currentUser = provider.getProfileByUID(mAuth.getCurrentUser().getUid());
            for (UserProfile p: profiles){
                if (currentUser.getFollowing().contains(p.getUID())){
                    for (MoodEvent event: p.getHistory().getEvents()){
                        if (event.isPublic()){
                            followingEvents.add(event);
                        }
                    }
                }
            }
        }

        followingEvents.sort((a,b)-> b.getDate().compareTo(a.getDate()));

    }

    private void followUser(String targetUserId) {
        // Log that the method was invoked.
        Log.d("FollowDebug", "followUser called with targetUserId: " + targetUserId);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d("FollowDebug", "No logged in user found.");
            Toast.makeText(getContext(), "No logged in user.", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUid = currentUser.getUid();
        Log.d("FollowDebug", "Current user UID: " + currentUid);

        // Prevent following yourself.
        if (currentUid.equals(targetUserId)) {
            Toast.makeText(getContext(), "You cannot follow yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug log to ensure targetUserId is valid.
        Log.d("FollowDebug", "Trying to follow UID: " + targetUserId);

        // Retrieve the current user's username (for logging purposes only; follow request uses UID).
        String currentUsername = "Unknown";
        UserProfile myProfile = ProfileProvider.getInstance(db).getProfileByUID(currentUid);
        if (myProfile != null && myProfile.getUsername() != null) {
            currentUsername = myProfile.getUsername();
            Log.d("FollowDebug", "Current user's username: " + currentUsername);
        } else {
            Log.d("FollowDebug", "Profile for current user not found or username is null.");
        }

        // Create a new FollowRequest object using the current user's UID for both fields.
        FollowRequest request = new FollowRequest(currentUid, currentUid, "pending");

        // Write the follow request to the target user's "requests" subcollection.
        db.collection("users")
                .document(targetUserId)
                .collection("requests")
                .document(currentUid)
                .set(request)
                .addOnSuccessListener(aVoid -> {

                    // Use this (the activity context) instead of getContext().
                    Toast.makeText(HomeActivity.this, "Follow request sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(HomeActivity.this, "Failed to send follow request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncPendingMoodEventsIfOnline(); //syncs offline-added mood events when coming back to HomeActivity
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
                                int index = displayEvents.indexOf(moodEvent);
                                if (index != -1) {
                                    adapter.notifyItemChanged(index);
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
}

}