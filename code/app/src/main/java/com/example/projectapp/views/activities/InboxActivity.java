package com.example.projectapp.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapp.views.adapters.FollowViewHolder;
import com.example.projectapp.R;
import com.example.projectapp.models.FollowRequest;
import com.example.projectapp.models.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the logged-in user's follow requests in a RecyclerView.
 * Each item can be accepted or declined.
 */
public class InboxActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowRequestAdapter adapter;
    private List<FollowRequest> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_inbox);

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_inbox); // Highlight current screen
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_inbox) {
                return true; // Already here
            } else if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (item.getItemId() == R.id.nav_map) {
                intent = new Intent(this, MapActivity.class);
            } else if (item.getItemId() == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
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

        // Initialize RecyclerView for follow requests
        recyclerView = findViewById(R.id.recycler_inbox);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FollowRequestAdapter(requestList);
        recyclerView.setAdapter(adapter);

        // Load follow requests from Firestore
        loadFollowRequests();
    }

    /**
     * Loads follow requests from the currently logged in user's "requests" subcollection.
     */
    private void loadFollowRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .collection("requests")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("InboxActivity", "Error loading follow requests: " + error.getMessage());
                        return;
                    }

                    List<FollowRequest> requests = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            FollowRequest req = doc.toObject(FollowRequest.class);
                            requests.add(req);
                        }
                    }

                    // Update the RecyclerView with the new list
                    requestList.clear();
                    requestList.addAll(requests);
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Called when user taps Accept on a follow request.
     */
    public void onAcceptClicked(FollowRequest request) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String myUid = currentUser.getUid();     // The user receiving the request
        String requesterUid = request.getFromUid(); // The user who sent the request

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(requesterUid)
                .get()
                .addOnSuccessListener(docSnapshot -> {
                    if (!docSnapshot.exists()) return;
                    UserProfile requesterProfile = docSnapshot.toObject(UserProfile.class);
                    if (requesterProfile == null) return;

                    // Add myUid to their 'following' array if not already there
                    if (!requesterProfile.getFollowing().contains(myUid)) {
                        requesterProfile.getFollowing().add(myUid);
                    }

                    // Update Firestore with the new 'following' array
                    db.collection("users")
                            .document(requesterUid)
                            .set(requesterProfile)
                            .addOnSuccessListener(aVoid -> {
                                // Remove the request from my (the requested user's) subcollection
                                db.collection("users")
                                        .document(myUid)
                                        .collection("requests")
                                        .document(requesterUid)
                                        .delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(this, "Follow request accepted!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("InboxActivity", "Error removing request doc: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("InboxActivity", "Error updating requester profile: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("InboxActivity", "Error loading requester profile: " + e.getMessage());
                });
    }

    /**
     * Called when user taps Decline on a follow request.
     */
    public void onDeclineClicked(FollowRequest request) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String myUid = currentUser.getUid();
        String requesterUid = request.getFromUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(myUid)
                .collection("requests")
                .document(requesterUid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Follow request declined.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("InboxActivity", "Error declining request: " + e.getMessage());
                });
    }

    /**
     * A simple RecyclerView.Adapter to display FollowRequest items.
     * Uses FollowViewHolder for Accept/Decline.
     */
    private class FollowRequestAdapter extends RecyclerView.Adapter<FollowViewHolder> {
        private List<FollowRequest> requests;

        public FollowRequestAdapter(List<FollowRequest> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // We'll inflate item_follow_request.xml
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_follow_request, parent, false);
            return new FollowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
            FollowRequest request = requests.get(position);
            holder.bind(request);
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }
    }
}
