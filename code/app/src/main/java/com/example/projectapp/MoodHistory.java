// -----------------------------------------------------------------------------
// File: MoodHistory.java
// -----------------------------------------------------------------------------
// This file defines the MoodHistory class, which serves as a model to manage a
// collection of MoodEvent objects in the ProjectApp. It handles storage,
// retrieval, and filtering of mood events.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * A class to hold a user's mood history.
 */
public class MoodHistory {
    private String userId;
    private ArrayList<MoodEvent> events;

    /**
     * Constructor for MoodHistory class.
     */
    public MoodHistory(){
        this.events = new ArrayList<MoodEvent>();
    }

    /**
     * Adds a given MoodEvent to the history and sorts the events in reverse chronological order.
     *
     * @param m the MoodEvent to be added
     */
    public void addEvent(MoodEvent m){
        this.events.add(m);
        Collections.sort(this.events);
    }

    /**
     * Deletes a given MoodEvent from the history and sorts the events in reverse chronological order.
     *
     * @param m the MoodEvent to be deleted
     * @throws IllegalArgumentException if the MoodEvent is not present in the history
     */
    public void deleteEvent(MoodEvent m){
        if (this.events.contains(m)){
            this.events.remove(m);
            Collections.sort(this.events);
        } else {
            throw new IllegalArgumentException("History does not contain that MoodEvent");
        }
    }

    /**
     * Returns the list of MoodEvent objects in this history.
     *
     * @return an ArrayList of MoodEvent objects
     */
    public ArrayList<MoodEvent> getEvents() {
        return this.events;
    }

    /**
     * Replaces the current list of events with a new list and sorts them in reverse chronological order.
     *
     * @param newEvents the new ArrayList of MoodEvent objects to set
     */
    public void setEvents(ArrayList<MoodEvent> newEvents) {
        this.events = newEvents;
        Collections.sort(this.events);
    }

    /**
     * Checks if the history contains a given MoodEvent.
     *
     * @param m the MoodEvent to check
     * @return true if the event is in the history, false otherwise
     */
    public boolean contains(MoodEvent m){
        return this.events.contains(m);
    }

    /**
     * Returns the user ID associated with this mood history.
     *
     * @return the user ID as a String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID for this mood history.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }


    public static Boolean isWithinPastWeek(Date eventDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date one_week_ago = calendar.getTime();
        return eventDate.after(one_week_ago);
    }

    public static Boolean matchesFilter(MoodEvent event, String selected_filter, String keyword) {
        switch(selected_filter) {
            case "Just Me":
            case "Both":
            case "Just People I'm Following":
            case "No Filter":
                return true;
            case "Past Week":
                return isWithinPastWeek(event.getDate());

            case "Emotional State":
                return event.getEmotionalState().toLowerCase().contains(keyword.toLowerCase());

            case "Reason Contains":
                return event.getReason() != null && event.getReason().toLowerCase().contains(keyword.toLowerCase());

            default:
                return false;
        }
    }

    public MoodHistory getFilteredVersion(ArrayList<String> filters){
        MoodHistory filteredHistory = new MoodHistory();
        for (MoodEvent moodEvent: events){
            boolean shouldBeIncluded = true;
            for (String filter: filters){
                 if (filter.startsWith("Emotional State") || filter.startsWith("Reason Contains")){
                     String[] split = filter.split(":");
                     String filterCategory = split[0];
                     String filterKeyword = split[1];
                     shouldBeIncluded = shouldBeIncluded && matchesFilter(moodEvent, filterCategory, filterKeyword);
                 }

            }

            if (shouldBeIncluded){
                filteredHistory.addEvent(moodEvent);
            }

        }

        return filteredHistory;
    }

}
