package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private ProfileProvider profileProvider;
    private static final String TAG = "SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Setting content view to activity_search");
        try {
            setContentView(R.layout.activity_search);
        } catch (Exception e) {
            Log.e(TAG, "Error setting content view", e);
            Toast.makeText(this, "Error loading layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Users");
        }

        Log.d(TAG, "onCreate: Initializing views");
        searchInput = findViewById(R.id.search_input);
        recyclerViewUsers = findViewById(R.id.recycler_view_users);

        if (searchInput == null) {
            Log.e(TAG, "search_input not found in layout");
            Toast.makeText(this, "Search input field not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (recyclerViewUsers == null) {
            Log.e(TAG, "recycler_view_users not found in layout");
            Toast.makeText(this, "RecyclerView not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize RecyclerView
        Log.d(TAG, "onCreate: Setting up RecyclerView");
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, new ArrayList<>());
        recyclerViewUsers.setAdapter(userAdapter);

        // Initialize ProfileProvider
        Log.d(TAG, "onCreate: Initializing ProfileProvider");
        profileProvider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());

        // Listen for profile updates and filter based on search input
        Log.d(TAG, "onCreate: Setting up ProfileProvider listener");
        profileProvider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                Log.d(TAG, "Profile data updated, filtering users...");
                filterUsers(searchInput.getText().toString());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching profiles: " + error);
                Toast.makeText(SearchActivity.this, "Error loading users: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Add TextWatcher to filter users as the user types
        Log.d(TAG, "onCreate: Setting up TextWatcher");
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Search query changed: " + s.toString());
                filterUsers(s.toString());
            }
        });
    }

    private void filterUsers(String query) {
        List<UserProfile> filteredUsers = new ArrayList<>();
        List<UserProfile> allProfiles = profileProvider.getProfiles();

        if (allProfiles == null) {
            Log.e(TAG, "Profile list is null");
            userAdapter.updateUsers(filteredUsers);
            return;
        }

        for (UserProfile user : allProfiles) {
            if (user == null) {
                Log.w(TAG, "Found a null UserProfile in the list");
                continue;
            }

            String username = user.getUsername();
            if (username != null && !username.isEmpty() && username.toLowerCase().startsWith(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }

        Log.d(TAG, "Filtered users count: " + filteredUsers.size());
        userAdapter.updateUsers(filteredUsers);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp: Returning to HomeActivity");
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }
}