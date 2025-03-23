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

public class ProfileActivity extends AppCompatActivity  implements ProfileEditFragment.EditProfileListener {

    UserProfile profile;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile); // Must match the layout file


        ProfileProvider provider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                /*
                IMPORTANT: We can't do anything until firebase actually gives the profiles back
                 */

                Bundle info = getIntent().getExtras();
                if (info != null && info.getString("uid") != null){
                    profile = provider.getProfileByUID(info.getString("uid"));
                    Log.d("Testing", "Option 1");
                } else {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        profile = provider.getProfileByUID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Log.d("Testing", "Option 2");
                    }
                }

                showUsername();

                Button logOutButton = findViewById(R.id.logout_button);
                Button showStatsButton = findViewById(R.id.button_show_stats);
                Button editProfileButton = findViewById(R.id.edit_profile_button);
                Button followButton = findViewById(R.id.follow_profile_button);


                if (FirebaseAuth.getInstance().getCurrentUser() != null && profile.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    logOutButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    });

                    showStatsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent statsIntent = new Intent(ProfileActivity.this, StatsActivity.class);
                            startActivity(statsIntent);
                        }
                    });

                    editProfileButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            ProfileEditFragment.newInstance(profile)
                                    .show(getSupportFragmentManager(), "Edit Profile");
                        }
                    });

                    followButton.setEnabled(false);
                    followButton.setVisibility(View.GONE);

                } else {

                    // TODO: Implement follow button listener

                    logOutButton.setEnabled(false);
                    logOutButton.setVisibility(View.GONE);
                    showStatsButton.setEnabled(false);
                    showStatsButton.setVisibility(View.GONE);
                    editProfileButton.setEnabled(false);
                    editProfileButton.setVisibility(View.GONE);
                }


            }

            @Override
            public void onError(String error) {
                // nothing
            }
        });




        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile); // Highlight "Profile"
            bottomNav.setOnItemSelectedListener(item -> {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(this, ProfileActivity.class);
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
        username_text.setText(profile.getUsername());
    }

    @Override
    public void onProfileEdited(UserProfile profile) {
        String uid = profile.getUID();
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "User profile saved! events="
                                + profile.getHistory().getEvents().size()))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error saving user profile", e));
    }
}