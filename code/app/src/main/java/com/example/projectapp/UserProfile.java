package com.example.projectapp;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private MoodHistory history;
    private String username;
    private String name;
    private String uid;

    // For Firestore
    public UserProfile() {
        // needed
    }

    public UserProfile(String uid, String username, String name) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.history = new MoodHistory();
    }

    public MoodHistory getHistory() {
        return history;
    }
    public void setHistory(MoodHistory history) {
        this.history = history;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getUID() {
        return uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }
}
