package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Setting content view to activity_main");

        // Launch HomeActivity by default
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Launching HomeActivity");
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "onCreate: Saved instance state exists, skipping default launch");
        }

        // Initialize Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            Log.d(TAG, "onCreate: Bottom navigation found");
            bottomNav.setOnItemSelectedListener(item -> {
                Intent intent = null;
                Log.d(TAG, "onItemSelected: Item ID = " + item.getItemId());

                // Match selected item and create the respective Intent
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(this, HomeActivity.class);
                } else if (item.getItemId() == R.id.nav_map) {
                    intent = new Intent(this, MapActivity.class);
                } else if (item.getItemId() == R.id.nav_history) {
                    intent = new Intent(this, HistoryActivity.class);
                } else if (item.getItemId() == R.id.nav_inbox) {
                    intent = new Intent(this, InboxActivity.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(this, ProfileActivity.class);
                }

                // Launch the selected Activity if intent is not null
                if (intent != null) {
                    Log.d(TAG, "onItemSelected: Launching " + intent.getComponent().getShortClassName());
                    startActivity(intent);
                }

                return true;
            });
        } else {
            Log.e(TAG, "onCreate: Bottom navigation not found");
        }
    }
}