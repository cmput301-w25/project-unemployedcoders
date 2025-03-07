package com.example.projectapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A class to hold a user's mood history
 */
public class MoodHistory implements Serializable {
    private static MoodHistory instance;
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

    //we use this getInstance to return the singleton class instance to
    //be used throughout the app
    public static MoodHistory getInstance() {
        if (instance == null) {
            instance = new MoodHistory();
        }
        return instance;
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
