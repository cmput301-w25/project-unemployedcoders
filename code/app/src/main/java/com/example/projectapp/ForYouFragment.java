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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ForYouFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoodEventRecyclerAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        recyclerView = view.findViewById(R.id.recycler_public_moods);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty data
        adapter = new MoodEventRecyclerAdapter(getContext(), new ArrayList<>(), event -> {
            followUser(event.getuid());
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Firestore real-time listener for public events
        db.collection("moodEvents")
                .whereEqualTo("isPublic", true)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        List<MoodEvent> publicEvents = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            MoodEvent event = doc.toObject(MoodEvent.class);
                            if (event != null) {
                                publicEvents.add(event);
                            }
                        }
                        adapter.setData(publicEvents);
                    }
                });

        return view;
    }

    private void followUser(String userId) {
        // Follow user logic here (e.g., update Firestore for following list)
        Toast.makeText(getContext(), "Followed user: " + userId, Toast.LENGTH_SHORT).show();
    }
}
