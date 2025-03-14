// -----------------------------------------------------------------------------
// File: ProfileActivity.java
// -----------------------------------------------------------------------------
// This file defines the ProfileActivity class, which serves as the user profile
// screen in the ProjectApp. It sets up a BottomNavigationView for navigating
// between app sections and highlights the "Profile" tab. The activity follows
// the Model-View-Controller (MVC) pattern, acting as the controller.
// Additionally, it now includes a "Show Stats" button that launches the StatsActivity.
// -----------------------------------------------------------------------------

package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectapp.HistoryActivity;
import com.example.projectapp.HomeActivity;
import com.example.projectapp.InboxActivity;
import com.example.projectapp.MapActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        showUsername();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile); // Must match the layout file

        showUsername();

        // Logout Button
        Button logOutButton = findViewById(R.id.logout_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Show Stats Button: Launches StatsActivity
        Button showStatsButton = findViewById(R.id.button_show_stats);
        showStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statsIntent = new Intent(ProfileActivity.this, StatsActivity.class);
                startActivity(statsIntent);
            }
        });

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

    private void showUsername() {
        TextView usernameText = findViewById(R.id.profile_username);
        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                String usernameStr = (userProfile.getUsername() == null) ? "Username" : userProfile.getUsername();
                usernameText.setText(usernameStr);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
