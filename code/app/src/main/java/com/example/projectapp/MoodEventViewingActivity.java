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

public class MoodEventViewingActivity extends AppCompatActivity {
    private ArrayList<MoodEvent> moodDataList;
    private ListView moodEventList;
    private MoodEventArrayAdapter moodEventAdapter;

    private void onMoodEdited(MoodEvent moodEvent) {}

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.mood_viewing_activity);

        String[] emotionalStates = {"Happiness", "Anger", "Shame"};
        //dummy data initalization
        moodDataList = new ArrayList<>();
        for (int i = 0; i < emotionalStates.length; i++) {
            moodDataList.add(new MoodEvent(emotionalStates[i]));
        }
        moodEventList = findViewById(R.id.user_mood_event_list);
        moodEventAdapter = new MoodEventArrayAdapter(this, moodDataList);
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
