package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_history);

        recyclerView = findViewById(R.id.history_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoodHistoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Initialize bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
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

        // Fetch mood history from Firebase
        FirebaseSync.getInstance().fetchMoodHistory(new MoodHistoryCallback() {
            @Override
            public void onMoodHistoryLoaded(MoodHistory history) {
                adapter.setData(history.getEvents());
                // Update UI with fetched data
                // myAdapter.setData(history.getEvents());
                // myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
