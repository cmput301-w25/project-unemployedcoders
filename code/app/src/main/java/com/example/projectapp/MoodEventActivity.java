package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Button;

public class MoodEventActivity extends AppCompatActivity {

    private Spinner spinnerEmotionalState;
    private EditText editBriefExplanation;
    private EditText editTrigger;
    private Spinner spinnerSocialSituation;
    private Button buttonUploadPhoto;
    private Button buttonAddLocation;
    private Button buttonAddEvent;
    private Button buttonViewMap; // New button for viewing map

    private LatLng eventLocation; // Store selected event location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_event);

        // Bind UI elements
        spinnerEmotionalState = findViewById(R.id.spinner_emotional_state);
        editBriefExplanation = findViewById(R.id.edit_brief_explanation);
        editTrigger = findViewById(R.id.edit_trigger);
        spinnerSocialSituation = findViewById(R.id.spinner_social_situation);
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonAddLocation = findViewById(R.id.button_add_location);
        buttonAddEvent = findViewById(R.id.button_add_event);
        buttonViewMap = findViewById(R.id.button_view_map);
        buttonAddLocation.setOnClickListener(view -> {
            // Launch a map picker or retrieve current location (dummy location used here)
            eventLocation = new LatLng(37.7749, -122.4194); // Example: San Francisco
            Toast.makeText(this, "Location Added!", Toast.LENGTH_SHORT).show();
        });
        // Setup button listener for viewing map
        buttonViewMap.setOnClickListener(view -> {
            if (eventLocation == null) {
                Toast.makeText(this, "No location added!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MoodEventActivity.this, MapViewActivity.class);
            intent.putExtra("latitude", eventLocation.latitude);
            intent.putExtra("longitude", eventLocation.longitude);
            startActivity(intent);
        });
        buttonAddEvent.setOnClickListener(view -> {
            String emotionalStateString = spinnerEmotionalState.getSelectedItem().toString();
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();

            // Validate and create a MoodEvent
            if (!MoodEvent.validTrigger(trigger)) {
                Toast.makeText(this, "Trigger is invalid. (<=20 chars, <=3 words)", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                MoodEvent newEvent = new MoodEvent(emotionalStateString, trigger, socialSituation);
                Toast.makeText(this, "Mood Event Added!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom Navigation



        // In your onCreate() method:
        String[] emotionalStates = getResources().getStringArray(R.array.emotional_states);
        MoodSpinnerAdapter adapter = new MoodSpinnerAdapter(this, R.layout.spinner_item, emotionalStates);
        spinnerEmotionalState.setAdapter(adapter);




        // Setup button listener for photo upload (optional)
        buttonUploadPhoto.setOnClickListener(view -> {
            // Code to open a file picker or camera
        });

        // Setup button listener for adding location (optional)
        buttonAddLocation.setOnClickListener(view -> {
            // Code to get or pick a location
        });

        // Listener for the "Add Event" button
        buttonAddEvent.setOnClickListener(view -> {
            // 1. Retrieve values from UI
            String emotionalStateString = spinnerEmotionalState.getSelectedItem().toString();
            String briefExplanation = editBriefExplanation.getText().toString().trim();
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();

            // Convert the selected mood string to a MoodType enum
            MoodType moodType = MoodType.fromString(emotionalStateString);
            if (moodType == null) {
                Toast.makeText(this, "Selected mood is not recognized.", Toast.LENGTH_SHORT).show();
                return;
            }
            String moodColor = moodType.getColorCode();
            int emoticonResId = moodType.getEmoticonResId();

            // 2. Validate input (e.g., validate trigger length)
            if (!MoodEvent.validTrigger(trigger)) {
                Toast.makeText(this, "Trigger is invalid. (<=20 chars, <=3 words)", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Create and add MoodEvent
            try {
                // Create a new MoodEvent (ensure your MoodEvent class supports moodColor and emoticonResId)
                MoodEvent newEvent = new MoodEvent(emotionalStateString, trigger, socialSituation);
                newEvent.setMoodColor(moodColor);
                newEvent.setEmoticonResId(emoticonResId);
                // Optionally, store the brief explanation if your model supports it
                // newEvent.setBriefExplanation(briefExplanation);

                // 4. Save to MoodHistory or via ViewModel/Repository (not shown here)
                // e.g., moodHistory.addEvent(newEvent);

                // 5. Notify user and finish activity
                Toast.makeText(this, "Mood Event Added! Color: " + moodColor, Toast.LENGTH_SHORT).show();
                finish();  // or navigate to another screen
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Invalid input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Bottom Navigation Listener
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Open home screen
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

