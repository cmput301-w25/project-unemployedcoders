package com.example.projectapp;

public interface UserProfileCallback {
    void onUserProfileLoaded(UserProfile userProfile);
    void onFailure(Exception e);
}