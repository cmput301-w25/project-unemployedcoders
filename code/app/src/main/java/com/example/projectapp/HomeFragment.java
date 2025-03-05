package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewMoodEvents;
    private TextView tabForYou, tabFollowing;
    private MoodEventRecyclerAdapter adapter;
    private List<MoodEvent> forYouEvents;
    private List<MoodEvent> followingEvents;
    private Button addEventButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerViewMoodEvents = view.findViewById(R.id.recycler_view_mood_events);
        tabForYou = view.findViewById(R.id.tab_for_you);
        tabFollowing = view.findViewById(R.id.tab_following);
        addEventButton = view.findViewById(R.id.add_event_button);


        // Set up adapter
        adapter = new MoodEventRecyclerAdapter(requireContext(), forYouEvents, followingEvents, new MoodEventRecyclerAdapter.OnMoodEventClickListener() {
            @Override
            public void onEditMoodEvent(MoodEvent event, int position) {
                // Implement edit logic
            }

            @Override
            public void onDeleteMoodEvent(MoodEvent event, int position) {
                // Implement delete logic
            }
        });
        recyclerViewMoodEvents.setAdapter(adapter);
        recyclerViewMoodEvents.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set click listeners for tabs
        tabForYou.setOnClickListener(v -> {
            tabForYou.setTextColor(getResources().getColor(android.R.color.white));
            tabFollowing.setTextColor(getResources().getColor(android.R.color.darker_gray));
            adapter.switchTab(0, forYouEvents);
        });

        tabFollowing.setOnClickListener(v -> {
            tabFollowing.setTextColor(getResources().getColor(android.R.color.white));
            tabForYou.setTextColor(getResources().getColor(android.R.color.darker_gray));
            adapter.switchTab(1, followingEvents);
        });

        // Set click listener for Add Event button
        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoodEventActivity.class);
            startActivity(intent);
        });

        // Set default tab
        tabForYou.performClick();

        return view;
    }
}