package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements MoodEventArrayAdapter.OnMoodEventClickListener,
MoodEventDetailsAndEditingFragment.EditMoodEventListener, MoodEventDeleteFragment.DeleteMoodEventDialogListener{
    private MoodHistory moodHistory;
    private ListView moodEventList;
    private MoodEventArrayAdapter moodEventAdapter;

    @Override
    public void onMoodEventEdited(MoodEvent moodEvent) {

        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                userProfile.setHistory(moodHistory);
                //moodEventAdapter.notifyDataSetChanged();
                fb.storeUserData(userProfile);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public void onEditMoodEvent(MoodEvent moodEvent, int position) {
        MoodEventDetailsAndEditingFragment.newInstance(moodEvent)
                .show(getSupportFragmentManager(), "Mood Event Details");
    }

    @Override
    public void onMoodEventDeleted(MoodEvent moodEvent) {

        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                moodHistory.deleteEvent(moodEvent);
                userProfile.setHistory(moodHistory);
                //moodEventAdapter.notifyDataSetChanged();
                fb.storeUserData(userProfile);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onDeleteMoodEvent(MoodEvent moodEvent, int position) {
        MoodEventDeleteFragment.newInstance(moodEvent).show(getSupportFragmentManager(), "DeleteMoodEvent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (moodEventAdapter != null){
            moodEventAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.mood_viewing_activity);


        moodEventList = findViewById(R.id.user_mood_event_list);

        FirebaseSync fb = FirebaseSync.getInstance();
        // Fetch mood history from Firebase
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                moodHistory = userProfile.getHistory();
                moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), moodHistory.getEvents(), HistoryActivity.this);
                moodEventList.setAdapter(moodEventAdapter);
                moodEventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        fb.listenForUpdates(new FirebaseSync.DataStatus() {
            @Override
            public void onDataUpdated() {
                fb.fetchUserProfileObject(new UserProfileCallback() {
                    @Override
                    public void onUserProfileLoaded(UserProfile userProfile) {
                        moodHistory = userProfile.getHistory();
                        moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), moodHistory.getEvents(), HistoryActivity.this);
                        moodEventList.setAdapter(moodEventAdapter);
                        moodEventAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Movie Update Error", error);
            }
        });

        // Initialize bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_mood_event_list);
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_history) {
                return true; // Already on History
            } else if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (item.getItemId() == R.id.nav_map) {
                intent = new Intent(this, MapActivity.class);
            } else if (item.getItemId() == R.id.nav_inbox) {
                intent = new Intent(this, InboxActivity.class);
            } else if (item.getItemId() == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
            return true;
        });


    }
}
