// -----------------------------------------------------------------------------
// File: MoodHistory.java
// -----------------------------------------------------------------------------
// This file defines the MoodHistory class, which likely serves as a model or
// utility class to manage a collection of MoodEvent objects in the ProjectApp.
// It may handle storage, retrieval, or filtering of mood events.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:

// -----------------------------------------------------------------------------
package com.example.projectapp;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A class to hold a user's mood history
 */
public class MoodHistory {
    private ArrayList<MoodEvent> events;

    /**
     * Constructor for MoodHistory class
     */
    public MoodHistory(){
        this.events = new ArrayList<MoodEvent>();
    }

    /**
     * This adds a given mood event to the history and sorts it in reverse chronological order
     * @param m
     *      The mood event to be added
     */
    public void addEvent(MoodEvent m){
        this.events.add(m);
        Collections.sort(this.events);
    }

    /**
     * This deletes a given mood event from the history and sorts it in reverse chronological order
     * @param m
     *      The mood event to be deleted
     */
    public void deleteEvent(MoodEvent m){
        if (this.contains(m)){
            this.events.remove(m);
            Collections.sort(this.events); // might be unnecessary
        } else {
            throw new IllegalArgumentException("History does not contain that MoodEvent");
        }

    }

    //getter that returns the list of events in the class
    public ArrayList<MoodEvent> getEvents() {
        return this.events;
    }

    /**
     * This checks if a given mood event is in the history
     * @param m
     *      The mood event to be checked
     * @return
     *      Whether or not the mood event is in the history
     */
    public boolean contains(MoodEvent m){
        return this.events.contains(m);
    }



}
