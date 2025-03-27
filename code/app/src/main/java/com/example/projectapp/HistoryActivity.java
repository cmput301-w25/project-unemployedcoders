// -----------------------------------------------------------------------------
// File: HistoryActivity.java
// -----------------------------------------------------------------------------
// This file defines the HistoryActivity class, which serves as the user mood
// history screen in the ProjectApp application. It fetches the user's mood
// history from Firebase, displays the events using a custom adapter in a ListView,
// and listens for real-time updates. Additionally, it implements editing and
// deletion functionality for individual mood events via dialog fragments.
//
// Design Pattern: MVC (Controller)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------

package com.example.projectapp;

import static com.example.projectapp.MoodHistory.matchesFilter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements MoodEventArrayAdapter.OnMoodEventClickListener,
MoodEventDetailsAndEditingFragment.EditMoodEventListener, MoodEventDeleteFragment.DeleteMoodEventDialogListener{
    private MoodHistory moodHistory;
    private ListView moodEventList;
    private MoodEventArrayAdapter moodEventAdapter;
    private EditText filterKeywordInput;
    private Button filterApplyButton;

    private ArrayList<MoodEvent> displayedMoodEvents;

    private List<MoodEvent> pendingEdits = new ArrayList<>();
    private List<MoodEvent> pendingDeletes = new ArrayList<>();

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    public void onMoodEventEdited(MoodEvent moodEvent) {
        if (isNetworkAvailable()) {
            syncEditedEvent(moodEvent);
        } else {
            pendingEdits.add(moodEvent);
            Toast.makeText(this, "No Internet, changes will sync when reconnected", Toast.LENGTH_SHORT).show();
        }
    }
    private void syncEditedEvent(MoodEvent moodEvent) {
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
        if (isNetworkAvailable()) {
            syncDeletedEvent(moodEvent);
        } else {
            pendingDeletes.add(moodEvent);
            Toast.makeText(this, "No internet, deletion will sync when reconnected", Toast.LENGTH_SHORT).show();
        }
    }

    private void syncDeletedEvent(MoodEvent moodEvent) {
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
        if (isNetworkAvailable()) {
            for (MoodEvent event : pendingEdits) {
                syncEditedEvent(event);
            }
            pendingEdits.clear();

            for (MoodEvent event : pendingDeletes) {
                syncDeletedEvent(event);
            }
            pendingDeletes.clear();
        }
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
        Spinner filter_spinner = findViewById(R.id.history_viewing_filter_spinner);
        ArrayAdapter<CharSequence> filter_spinner_adapter = ArrayAdapter.createFromResource(
                this,
                R.array.history_activity_filter_choices,
                android.R.layout.simple_spinner_item
        );
        filterKeywordInput = findViewById(R.id.history_viewing_filter_keyword_input);
        filterApplyButton = findViewById(R.id.history_viewing_filter_apply_button);

        filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected_filter = parent.getItemAtPosition(position).toString();

                if (selected_filter.equals("Emotional State")) {
                    filterKeywordInput.setVisibility(android.view.View.VISIBLE);
                } else if (selected_filter.equals("Reason Contains")) {
                    filterKeywordInput.setVisibility(android.view.View.VISIBLE);
                } else {
                    filterKeywordInput.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                filterKeywordInput.setVisibility(android.view.View.GONE);
            }
        });

        FirebaseSync fb = FirebaseSync.getInstance();
        // Fetch mood history from Firebase
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                moodHistory = userProfile.getHistory();
                displayedMoodEvents = moodHistory.getEvents();
                moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), displayedMoodEvents, HistoryActivity.this);
                moodEventList.setAdapter(moodEventAdapter);
                moodEventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // 1 time
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
                        filter_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        filter_spinner.setAdapter(filter_spinner_adapter);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Update Error", error);
            }
        });

        filterApplyButton.setOnClickListener(view -> {
            String selected_filter = filter_spinner.getSelectedItem().toString();
            String keyword = filterKeywordInput.getText().toString().trim();

            if ((selected_filter.equals("Emotional State") || selected_filter.equals("Reason Contains")) && keyword.isEmpty())
            {

                Snackbar.make(view, "Please enter a keyword for this filter", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (moodHistory != null) {
                ArrayList<MoodEvent> filteredEvents = new ArrayList<>();

                for (MoodEvent event : moodHistory.getEvents()) {
                    if (MoodHistory.matchesFilter(event, selected_filter, keyword)) {
                        filteredEvents.add(event);
                    }
                }

                moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), filteredEvents, HistoryActivity.this);
                moodEventList.setAdapter(moodEventAdapter);
                moodEventAdapter.notifyDataSetChanged();
                String message;
                if (selected_filter.equals("No Filter")) {
                    message = "All events shown.";
                } else if (selected_filter.equals("Emotional State")) {
                    message = "Filter applied: " + selected_filter + " is " + keyword;
                } else if (selected_filter.equals("Reason Contains")) {
                    message = "Filter applied: " + selected_filter + " " + keyword;
                } else {
                    message = "Filter applied: " + selected_filter;
                }
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
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
