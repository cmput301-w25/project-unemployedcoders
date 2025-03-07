// -----------------------------------------------------------------------------
// File: MoodEventViewingActivity.java
// -----------------------------------------------------------------------------
// This file defines the MoodEventViewingActivity class, which serves as a screen
// in the ProjectApp to view and manage a list of MoodEvent objects using a ListView.
// It implements interfaces for editing and deleting mood events via dialog fragments
// and includes a BottomNavigationView for navigation. The activity follows the
// Model-View-Controller (MVC) pattern, acting as the controller.
//
// Design Pattern: MVC (Controller)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MoodEventViewingActivity extends AppCompatActivity implements MoodEventArrayAdapter.OnMoodEventClickListener,
        MoodEventDetailsAndEditingFragment.EditMoodEventListener,
        MoodEventDeleteFragment.DeleteMoodEventDialogListener{
    private MoodHistory moodHistory;
    private ListView moodEventList;
    private MoodEventArrayAdapter moodEventAdapter;

    @Override
    public void onMoodEventEdited(MoodEvent moodEvent) {
        moodEventAdapter.notifyDataSetChanged();
    }
    @Override
    public void onEditMoodEvent(MoodEvent moodEvent, int position) {
        MoodEventDetailsAndEditingFragment.newInstance(moodEvent)
                .show(getSupportFragmentManager(), "Mood Event Details");
    }

    @Override
    public void onMoodEventDeleted(MoodEvent moodEvent) {
        moodHistory.deleteEvent(moodEvent);
        moodEventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteMoodEvent(MoodEvent moodEvent, int position) {
        MoodEventDeleteFragment.newInstance(moodEvent).show(getSupportFragmentManager(), "DeleteMoodEvent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        moodEventAdapter.notifyDataSetChanged();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.mood_viewing_activity);

        String[] emotionalStates = {"Happiness", "Anger", "Shame"};
        String[] triggers = {"Apple", "Apple", "Apple"};
        String[] socialSituations = {"Alone", "Alone", "Alone"};
        moodHistory = MoodHistory.getInstance();
        moodEventList = findViewById(R.id.user_mood_event_list);
        moodEventAdapter = new MoodEventArrayAdapter(this, moodHistory.getEvents(), this);
        moodEventList.setAdapter((moodEventAdapter));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_mood_event_list);
        //set the bottom nav bar to the nav history since that is how we initially got here
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Open home screen
                startActivity(new Intent(MoodEventViewingActivity.this, MoodEventActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                // Open map screen
                return true;
            } else if (id == R.id.nav_history) {
                // Open mood history screen
                return true;
            } else if (id == R.id.nav_inbox) {
                // Open inbox screen
                return true;
            } else if (id == R.id.nav_profile) {
                // Open Profile screen
                return true;
            }
            return false;
        });


    }
}
