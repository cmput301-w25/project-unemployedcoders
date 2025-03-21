package com.example.projectapp;

import java.util.ArrayList;
import java.util.Collections;

public class MoodHistory {
    private String userId;
    private ArrayList<MoodEvent> events;

    public MoodHistory() {
        this.events = new ArrayList<>();
    }

    public void addEvent(MoodEvent m) {
        this.events.add(m);
        Collections.sort(this.events);
    }

    public void deleteEvent(MoodEvent m) {
        if (this.events.contains(m)) {
            this.events.remove(m);
            Collections.sort(this.events);
        } else {
            throw new IllegalArgumentException("History does not contain that MoodEvent");
        }
    }

    public ArrayList<MoodEvent> getEvents() {
        return this.events;
    }

    // So we can replace the entire list after reading from Firestore
    public void setEvents(ArrayList<MoodEvent> newEvents) {
        this.events = newEvents;
        Collections.sort(this.events);
    }



    public boolean contains(MoodEvent m) { return this.events.contains(m); }
}
