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
                event -> followUser(event.getUsername()) // now follow uses the username
        );
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Use ProfileProvider to listen for updates on user profiles.
        ProfileProvider.getInstance(db).listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                // Get the cached list of user profiles from ProfileProvider.
                ArrayList<UserProfile> profiles = ProfileProvider.getInstance(db).getProfiles();
                List<MoodEvent> publicEvents = new ArrayList<>();

                for (UserProfile profile : profiles) {
                    if (profile != null && profile.getHistory() != null
                            && profile.getHistory().getEvents() != null) {
                        for (MoodEvent event : profile.getHistory().getEvents()) {
                            if (event != null && event.isPublic()) {
                                // Set the username from the profile so that the adapter can display it.
                                event.setUsername(profile.getUsername());
                                publicEvents.add(event);
                            }
                        }
                    }
                }
                // Sort events by date descending.
                Collections.sort(publicEvents, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                adapter.setData(publicEvents);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        return view;


    }



    private void followUser(String username) {
        Toast.makeText(getContext(), "Followed user: " + username, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Optionally remove any listeners if needed.
    }
}
