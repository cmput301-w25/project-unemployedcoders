package com.example.projectapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A class to model a user's profile.
 */
public class UserProfile implements Serializable {

    private MoodHistory history;
    private String username;
    private String name; // the user's actual name
    private String uid;
    private ArrayList<String> following = new ArrayList<>();

    public UserProfile(String uid, String username, String name) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.history = new MoodHistory();
    }

    public UserProfile() {
        // needed by Firestore
    }

    public void setHistory(MoodHistory history) {
        this.history = history;
    }

    public MoodHistory getHistory() {
        return history;
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

    public void setUID(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    /**
     * Returns the 3 most recent mood events from the user's history.
     * @return A list of up to 3 most recent MoodEvent objects, sorted by date (most recent first).
     */
    public List<MoodEvent> getRecentEvents() {
        List<MoodEvent> events = history.getEvents();
        if (events == null || events.isEmpty()) {
            return new ArrayList<>();
        }

        // Sort events by date (most recent first)
        Collections.sort(events, new Comparator<MoodEvent>() {
            @Override
            public int compare(MoodEvent e1, MoodEvent e2) {
                return e2.getDate().compareTo(e1.getDate()); // Assuming getDate() returns a Date object
            }
        });

        // Return up to 3 most recent events
        return events.size() > 3 ? events.subList(0, 3) : events;
    }
}