package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Setting content view to activity_main");

        // Load the Home Fragment by default
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Loading HomeFragment");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        } else {
            Log.d(TAG, "onCreate: Saved instance state exists, skipping fragment load");
        }

        // Initialize Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            Log.d(TAG, "onCreate: Bottom navigation found");
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                Log.d(TAG, "onItemSelected: Item ID = " + item.getItemId());

                // Match selected item and load the respective fragment
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_map) {
                    selectedFragment = new MapFragment();
                } else if (item.getItemId() == R.id.nav_history) {
                    selectedFragment = new HistoryFragment();
                } else if (item.getItemId() == R.id.nav_inbox) {
                    selectedFragment = new InboxFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                // Load the selected fragment if it's not null
                if (selectedFragment != null) {
                    Log.d(TAG, "onItemSelected: Replacing with " + selectedFragment.getClass().getSimpleName());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            });
        } else {
            Log.e(TAG, "onCreate: Bottom navigation not found");
        }
    }
}