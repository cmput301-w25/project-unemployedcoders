package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
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
        setContentView(R.layout.activity_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Users");
        }

        searchInput = findViewById(R.id.search_input);
        recyclerViewUsers = findViewById(R.id.recycler_view_users);
        Button backToHomeButton = findViewById(R.id.back_to_home_button);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, new ArrayList<>(), user -> {
            Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
            intent.putExtra("uid", user.getUID());
            intent.putExtra("fromSearch", true); // Ensure this flag is set
            startActivity(intent);
        });
        recyclerViewUsers.setAdapter(userAdapter);

        profileProvider = ProfileProvider.getInstance(FirebaseFirestore.getInstance());

        profileProvider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                filterUsers(searchInput.getText().toString());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SearchActivity.this, "Error loading users: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterUsers(s.toString());
            }
        });

        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void filterUsers(String query) {
        List<UserProfile> filteredUsers = new ArrayList<>();
        List<UserProfile> allProfiles = profileProvider.getProfiles();

        if (allProfiles == null) {
            userAdapter.updateUsers(filteredUsers);
            return;
        }

        for (UserProfile user : allProfiles) {
            if (user != null && user.getUsername() != null &&
                    user.getUsername().toLowerCase().startsWith(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }

        userAdapter.updateUsers(filteredUsers);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }
}