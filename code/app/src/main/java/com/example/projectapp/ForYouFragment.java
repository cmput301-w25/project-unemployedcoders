package com.example.projectapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForYouFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoodEventRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        recyclerView = view.findViewById(R.id.recycler_public_moods);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MoodEventRecyclerAdapter(
                getContext(),
                new ArrayList<>(),
                event -> followUser(event.getUserId())
        );
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Listen for changes in "users" collection
        listenerRegistration = db.collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;

                    List<MoodEvent> publicEvents = new ArrayList<>();

                    for (DocumentSnapshot userDoc : snapshots.getDocuments()) {
                        UserProfile userProfile = userDoc.toObject(UserProfile.class);
                        if (userProfile != null && userProfile.getHistory() != null) {
                            List<MoodEvent> events = userProfile.getHistory().getEvents();
                            if (events != null) {
                                // Filter: only show events where isPublic == true
                                for (MoodEvent e : events) {
                                    if (e != null && e.isPublic()) {
                                        publicEvents.add(e);
                                    }
                                }
                            }
                        }
                    }

                    // Sort by date descending
                    Collections.sort(publicEvents, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));

                    // Update adapter
                    adapter.setData(publicEvents);
                });

        return view;
    }

    private void followUser(String userId) {
        Toast.makeText(getContext(), "Followed user: " + userId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
