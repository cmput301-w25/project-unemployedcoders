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
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

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

        Button logOutButton = findViewById(R.id.logout_button);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        Button showStatsButton = findViewById(R.id.button_show_stats);
        showStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statsIntent = new Intent(ProfileActivity.this, StatsActivity.class);
                startActivity(statsIntent);
            }
        });

        Button editProfileButton = findViewById(R.id.edit_profile_button);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db;
                db = FirebaseFirestore.getInstance();
                ProfileProvider p = ProfileProvider.getInstance(db);
                p.listenForUpdates(new ProfileProvider.DataStatus() {
                    @Override
                    public void onDataUpdated(ArrayList<UserProfile> profiles) {
                        for (UserProfile prof: profiles){
                            Log.d("Database", prof.getUsername());
                        }

                    }

                    @Override
                    public void onError(String error) {

                    }
                });

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

    private void showUsername(){
        TextView username_text = findViewById(R.id.profile_username);

        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                String username_str;
                if (userProfile.getUsername() == null){
                    username_str = "Username";
                } else {
                    username_str = userProfile.getUsername();
                }

                username_text.setText(username_str);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}