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

package com.example.projectapp.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.projectapp.database_util.FirebaseSync;
import com.example.projectapp.views.adapters.MoodEventArrayAdapter;
import com.example.projectapp.views.fragments.MoodEventDeleteFragment;
import com.example.projectapp.views.fragments.MoodEventDetailsAndEditingFragment;
import com.example.projectapp.R;
import com.example.projectapp.database_util.UserProfileCallback;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.MoodHistory;
import com.example.projectapp.models.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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

    private static final String PREFS_NAME = "OfflineSyncPrefs";
    private static final String KEY_UNSYNCED_EDITS = "unsyncedEdits";
    private static final String KEY_UNSYNCED_HISTORY = "unsyncedMoodHistory";
    private boolean suppressNextFirebaseUpdate = false;
    private int filterCount = 0;



    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void setUnsyncedEdits(boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_UNSYNCED_EDITS, value).apply();
    }

    private boolean getUnsyncedEdits() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_UNSYNCED_EDITS, false);
    }

    private void saveLocalMoodHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(moodHistory.getEvents());

        editor.putString(KEY_UNSYNCED_HISTORY, json);
        editor.apply();
    }

    private ArrayList<MoodEvent> loadLocalMoodEvents() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_UNSYNCED_HISTORY, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<MoodEvent>>() {}.getType();
            return gson.fromJson(json, type);
        }

        return null;
    }

    private void clearLocalMoodHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(KEY_UNSYNCED_HISTORY).apply();
    }


    @Override
    public void onMoodEventEdited(MoodEvent moodEvent) {
        if (isNetworkAvailable()) {
            syncEditedEvent(moodEvent);
        } else {
            Toast.makeText(this, "No Internet, changes will sync when reconnected", Toast.LENGTH_SHORT).show();
            setUnsyncedEdits(true);
            saveLocalMoodHistory();
        }

        if (moodEventAdapter != null) {
            moodEventAdapter.notifyDataSetChanged();
        }

        if (filterCount > 0) {
            Spinner filterSpinner = findViewById(R.id.history_viewing_filter_spinner);
            EditText filterKeywordInput = findViewById(R.id.history_viewing_filter_keyword_input);
            filterSpinner.setSelection(0); // Reset to "No Filter"
            filterKeywordInput.setText(""); // Clear any typed keyword
            filterCount = 0;
            Toast.makeText(this, "Any filters cleared due to edit", Toast.LENGTH_SHORT).show();
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

    private void listenForFirebaseUpdates() {
        FirebaseSync.getInstance().listenForUpdates(new FirebaseSync.DataStatus() {
            @Override
            public void onDataUpdated() {
                if (suppressNextFirebaseUpdate) {
                    suppressNextFirebaseUpdate = false;
                    return;
                }

                FirebaseSync.getInstance().fetchUserProfileObject(new UserProfileCallback() {
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
                        Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Update Error", error);
            }
        });
    }

    private void syncMoodHistory() {
        suppressNextFirebaseUpdate = true;

        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                // Load local edited mood history if it exists
                MoodHistory editedHistory;
                ArrayList<MoodEvent> localEvents = loadLocalMoodEvents();
                if (localEvents != null && !localEvents.isEmpty()) {
                    editedHistory = new MoodHistory();
                    editedHistory.setEvents(localEvents);
                } else {
                    editedHistory = userProfile.getHistory();
                }

                // Load offline additions
                SharedPreferences prefs = getSharedPreferences("PendingMoodEvents", MODE_PRIVATE);
                String json = prefs.getString("pendingMoodEvents", null);
                List<MoodEvent> pendingAdditions = new ArrayList<>();
                if (json != null) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<MoodEvent>>() {}.getType();
                    pendingAdditions = gson.fromJson(json, type);
                }

                // Merge additions into editedHistory
                for (MoodEvent event : pendingAdditions) {
                    editedHistory.addEvent(event);
                }

                // Save merged result
                userProfile.getHistory().setEvents(new ArrayList<>(editedHistory.getEvents()));
                fb.storeUserData(userProfile);

                // Clear local changes
                prefs.edit().remove("pendingMoodEvents").apply();
                clearLocalMoodHistory();
                setUnsyncedEdits(false);

                // Re-fetch to refresh UI
                fb.fetchUserProfileObject(new UserProfileCallback() {
                    @Override
                    public void onUserProfileLoaded(UserProfile updatedProfile) {
                        moodHistory = updatedProfile.getHistory();
                        displayedMoodEvents = moodHistory.getEvents();

                        if (moodEventAdapter == null) {
                            moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), displayedMoodEvents, HistoryActivity.this);
                            moodEventList.setAdapter(moodEventAdapter);
                        } else {
                            moodEventAdapter.clear();
                            moodEventAdapter.addAll(displayedMoodEvents);
                        }

                        moodEventAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(HistoryActivity.this, "Error reloading: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        try {
            moodHistory.deleteEvent(moodEvent);
        } catch (IllegalArgumentException e) {
            Log.w("MoodEventDelete", "Tried to delete an event that doesn't exist");
        }

        if (isNetworkAvailable()) {
            syncDeletedEvent(moodEvent);
        } else {
            setUnsyncedEdits(true);
            saveLocalMoodHistory();
            Toast.makeText(this, "No internet, deletion will sync when reconnected", Toast.LENGTH_SHORT).show();
        }

        if (moodEventAdapter != null) {
            moodEventAdapter.notifyDataSetChanged();
        }

        if (filterCount > 0) {
            Spinner filterSpinner = findViewById(R.id.history_viewing_filter_spinner);
            EditText filterKeywordInput = findViewById(R.id.history_viewing_filter_keyword_input);
            filterSpinner.setSelection(0); // Reset to "No Filter"
            filterKeywordInput.setText(""); // Clear any typed keyword
            filterCount = 0;
            Toast.makeText(this, "Any filters cleared due to edit", Toast.LENGTH_SHORT).show();
        }
    }

    private void syncDeletedEvent(MoodEvent moodEvent) {
        FirebaseSync fb = FirebaseSync.getInstance();
        fb.fetchUserProfileObject(new UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                try {
                    moodHistory.deleteEvent(moodEvent);
                } catch (IllegalArgumentException e) {
                    Log.d("syncDel", "event was already deleted");
                }
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
        SharedPreferences state = getSharedPreferences("OfflineSyncPrefs", MODE_PRIVATE);
        boolean justSyncedOfflineEvents = state.getBoolean("offlineEventsSynced", false);
        ArrayList<MoodEvent> localEvents = loadLocalMoodEvents();

        if (!isNetworkAvailable() && localEvents != null) {
            if (moodHistory == null) {
                moodHistory = new MoodHistory();
            }
            moodHistory.setEvents(localEvents);
            displayedMoodEvents = localEvents;

            if (moodEventAdapter == null) {
                moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), displayedMoodEvents, this);
                moodEventList.setAdapter(moodEventAdapter);
            } else {
                moodEventAdapter.clear();
                moodEventAdapter.addAll(displayedMoodEvents);
            }

            moodEventAdapter.notifyDataSetChanged();
            return;
        }

        if (justSyncedOfflineEvents || (isNetworkAvailable() && getUnsyncedEdits())) {
            state.edit().putBoolean("offlineEventsSynced", false).apply(); // Reset the flag

            if (moodHistory == null) moodHistory = new MoodHistory();

            if (localEvents != null) {
                moodHistory.setEvents(localEvents);
            }

            syncMoodHistory();
        } else {
            listenForFirebaseUpdates();
        }

        if (moodEventAdapter != null) {
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
        filter_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter_spinner.setAdapter(filter_spinner_adapter);

        ArrayList<MoodEvent> localEvents = loadLocalMoodEvents();

        if (getUnsyncedEdits() && localEvents != null) {
            moodHistory = new MoodHistory();
            moodHistory.setEvents(localEvents);
            displayedMoodEvents = localEvents;
            moodEventAdapter = new MoodEventArrayAdapter(getApplicationContext(), displayedMoodEvents, this);
            moodEventList.setAdapter(moodEventAdapter);
            moodEventAdapter.notifyDataSetChanged();
        } else {
            // Fetch moodhistory from Firebase
            FirebaseSync fb = FirebaseSync.getInstance();
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
                    Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        filterApplyButton.setOnClickListener(view -> {
            filterCount += 1;
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
                    filterCount = 0;
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