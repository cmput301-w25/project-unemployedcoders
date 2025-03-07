package com.example.projectapp;

public interface MoodHistoryCallback {
    void onMoodHistoryLoaded(MoodHistory history);
    void onFailure(Exception e);
}
