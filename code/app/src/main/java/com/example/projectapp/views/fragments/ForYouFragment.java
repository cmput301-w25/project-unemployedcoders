// -----------------------------------------------------------------------------
// File: ForYouFragment.java
// -----------------------------------------------------------------------------
// This fragment displays public MoodEvents from user profiles that are less
// than 30 minutes old. It listens for updates via ProfileProvider, filters out
// events older than 30 minutes, sorts them in reverse chronological order, and
// updates the RecyclerView adapter.
//
// Design Pattern: MVC (View)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.views.adapters.MoodEventRecyclerAdapter;
import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.R;
import com.example.projectapp.models.FollowRequest;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays public MoodEvents and allows sending follow requests.
 */


/*
https://developer.android.com/reference/android/os/Handler (Shubham)
ChatGPT helped me design this and gave me partial code as well at times to show me where i went wrong (Shubham)
 */
public class ForYouFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoodEventRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    // Keep a list of the latest unfiltered public events.
    private List<MoodEvent> latestPublicEvents = new ArrayList<>();

    // Handler & Runnable for periodic re-check.
    private static final long REFRESH_INTERVAL_MS = 60_000; // 1 minute
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        recyclerView = view.findViewById(R.id.recycler_public_moods);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // When the follow button is clicked, we now use the event's userId.
        adapter = new MoodEventRecyclerAdapter(
                getContext(),
                new ArrayList<>(),
                event -> followUser(event.getUserId())
        );
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Listen for updates on user profiles via ProfileProvider.
        ProfileProvider.getInstance(db).listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                ArrayList<UserProfile> profiles = ProfileProvider.getInstance(db).getProfiles();
                List<MoodEvent> publicEvents = new ArrayList<>();

                // Loop through each profile, and for each mood event in history…
                for (UserProfile profile : profiles) {
                    if (profile != null && profile.getHistory() != null &&
                            profile.getHistory().getEvents() != null) {
                        for (MoodEvent event : profile.getHistory().getEvents()) {
                            if (event != null && event.isPublic()) {
                                // CRUCIAL: Set both the userId and username from the profile.
                                event.setUserId(profile.getUID());
                                event.setUsername(profile.getUsername());
                                publicEvents.add(event);
                            }
                        }
                    }
                }

                // Save the unfiltered list for future time-based checks.
                latestPublicEvents = publicEvents;

                // Filter & sort events immediately.
                filterAndDisplayEvents(latestPublicEvents);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Filters out events older than 30 minutes, sorts them, and updates the adapter.
     *
     * @param events The list of unfiltered events.
     */
    private void filterAndDisplayEvents(List<MoodEvent> events) {
        long thirtyMinutesInMillis = 30 * 60 * 1000;
        long currentTime = System.currentTimeMillis();

        List<MoodEvent> filteredEvents = new ArrayList<>();
        for (MoodEvent event : events) {
            if (currentTime - event.getDate().getTime() <= thirtyMinutesInMillis) {
                filteredEvents.add(event);
            }
        }

        // Sort events by date descending.
        Collections.sort(filteredEvents, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
        adapter.setData(filteredEvents);
    }

    /**
     * Sends a follow request to the specified user.
     * A new FollowRequest is created in the target user's "requests" subcollection.
     *
     * @param targetUserId The UID of the user to follow.
     */
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
            Log.d("FollowDebug", "Blocked attempt to follow yourself. currentUid equals targetUserId.");
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
                    Log.d("FollowDebug", "Follow request successfully written to Firestore for targetUserId: " + targetUserId);
                    Toast.makeText(getContext(), "Follow request sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowDebug", "Failed to send follow request: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to send follow request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshRunnable = () -> {
            filterAndDisplayEvents(latestPublicEvents);
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
