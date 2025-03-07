// -----------------------------------------------------------------------------
// File: ProfileActivity.java
// -----------------------------------------------------------------------------
// This file defines the ProfileActivity class, which serves as the user profile
// screen in the ProjectApp. It sets up a BottomNavigationView for navigating
// between app sections and highlights the "Profile" tab. The activity follows
// the Model-View-Controller (MVC) pattern, acting as the controller.
//
// Design Pattern: MVC (Controller)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectapp.HistoryActivity;
import com.example.projectapp.HomeActivity;
import com.example.projectapp.InboxActivity;
import com.example.projectapp.MapActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile); // Must match the layout file

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile); // Highlight "Profile"
            bottomNav.setOnItemSelectedListener(item -> {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_profile) {
                    return true; // Already on Profile
                } else if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(this, HomeActivity.class);
                } else if (item.getItemId() == R.id.nav_map) {
                    intent = new Intent(this, MapActivity.class);
                } else if (item.getItemId() == R.id.nav_history) {
                    intent = new Intent(this, HistoryActivity.class);
                } else if (item.getItemId() == R.id.nav_inbox) {
                    intent = new Intent(this, InboxActivity.class);
                }

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                return true;
            });
        } else {
            android.util.Log.e("ProfileActivity", "BottomNavigationView not found");
        }
    }
}