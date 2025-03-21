// -----------------------------------------------------------------------------
// File: MoodEvent.java
// -----------------------------------------------------------------------------
// This file defines the MoodEvent class, a model class representing a single
// mood-related event in the ProjectApp. It captures details such as emotional
// state, trigger, social situation, date, and geolocation (latitude/longitude).
// The class implements Comparable for sorting events in reverse chronological
// order and Serializable for potential data persistence.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
//  N/A

// -----------------------------------------------------------------------------
package com.example.projectapp;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;
import android.net.Uri;

/**
 * This is a class that models a MoodEvent
 */
public class MoodEvent implements Comparable<MoodEvent>, Serializable {


    public static final String[] ALL_SITUATIONS = {"Alone" , "With one other person", "With two to several people", "With a crowd"};

    private Date date;
    private String emotionalState;
    private String userId;

    private String reason;
    private String trigger;
    private String socialSituation;

    private MoodType moodType;
    private double latitude;  // New: Store latitude
    private double longitude; // New: Store longitude

    private boolean isPublic;

    private Uri photoUri;


    public MoodEvent(String emotionalState, String reason, String socialSituation, Uri photoUri, String userId) {
        if (reason != null && !validReason(reason)) {
            throw new IllegalArgumentException("Not a valid reason");
        }
        if (MoodType.fromString(emotionalState) == null) {
            throw new IllegalArgumentException("Not a valid emotional state");
        }
        if (socialSituation != null && !Arrays.asList(ALL_SITUATIONS).contains(socialSituation)) {
            throw new IllegalArgumentException("Not a valid social situation");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.reason = reason;
        this.socialSituation = socialSituation;
        this.moodType = MoodType.fromString(emotionalState);
        this.photoUri = photoUri;
        this.userId = userId;
    }

    /**
     * This is one constructor for the MoodEvent class
     * @param emotionalState
     *      The specific emotional state of the event
     * @param reason
     *      The reason for the event
     * @param trigger
     *      The trigger of the event
     * @param socialSituation
     *      The social situation of the event
     */
    public MoodEvent(String emotionalState, String reason, String trigger, String socialSituation){
        if (!validReason(reason)){
            throw new IllegalArgumentException("Not a valid reason");
        }

        if (MoodType.fromString(emotionalState) == null){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        if (socialSituation != null && !Arrays.asList(ALL_SITUATIONS).contains(socialSituation)){
            throw new IllegalArgumentException("Not a valid social situation");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.trigger = trigger;
        this.reason = reason;
        this.socialSituation = socialSituation;
        this.moodType = MoodType.fromString(emotionalState);
        this.photoUri = photoUri;
    }

    public MoodEvent(){
        // For firebase deserialization
    }

    /**
     * This is one constructor for the MoodEvent class
     * @param emotionalState
     *      The specific emotional state of the event
     * @param reason
     *      The reason for the event
     */
    public MoodEvent(String emotionalState, String reason){
        if (MoodType.fromString(emotionalState) == null){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        if (!validReason(reason)){
            throw new IllegalArgumentException("Not a valid reason");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.reason = reason;
        this.trigger = null;
        this.socialSituation = null;
        this.moodType = MoodType.fromString(emotionalState);
    }

    /**
     * This returns the emotional state of the event
     * @return
     *      Returns the emotional state of the event
     */
    public String getEmotionalState() {
        return emotionalState;
    }

    /**
     * This sets the emotional state of the event
     * @param emotionalState
     *      The emotionalState to set for the event
     */
    public void setEmotionalState(String emotionalState) {
        if (MoodType.fromString(emotionalState) == null){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        this.emotionalState = emotionalState;
        this.moodType = MoodType.fromString(emotionalState);
    }

    /**
     * This returns the user id  of the user
     * @return
     *      Returns the user id of the user
     */

    public String getUserId() { return userId; }

    /**
     * This sets the user id of the user
     * @param userId
     *      The user id to set for the user
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * This returns the reason of the event
     * @return
     *      Returns the reason of the event
     */
    public String getReason() {
        return reason;
    }

    /**
     * This sets the reason of the event
     * @param reason
     *      The trigger to set for the event
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * This returns the social situation of the event
     * @return
     *      Returns the emotional state of the event
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * This sets the social situation of the event
     * @param socialSituation
     *      The social situation to set for the event
     */
    public void setSocialSituation(String socialSituation) {
        if (socialSituation == null){
            this.socialSituation = socialSituation;
        } else if (socialSituation.equals("Choose not to answer")){
            this.socialSituation = null;
        } else {
            this.socialSituation = socialSituation;
        }
    }

    /**
     * This returns the date of the event
     * @return
     *      Returns the date of the event
     */
    public Date getDate() {
        return date;
    }

    /**
     * This checks if a trigger is valid in length
     * @param reason
     *      The trigger to check
     * @return
     *      Whether or not the trigger is valid
     */
    public static boolean validReason(String reason){
        if (reason == null || reason.trim().isEmpty()){
            return false;
        }

        int wordCount = reason.trim().split(" ").length;
        return wordCount <= 3 && reason.length() <= 20;
    }



    /**
     * Returns the latitude for this mood event.
     * @return
     *      The latitude value (double)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude for this mood event.
     * @param latitude
     *      The latitude value (double)
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude for this mood event.
     * @return
     *      The longitude value (double)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude for this mood event.
     * @param longitude
     *      The longitude value (double)
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets the color resource associated with the mood
     * @return
     *      The color resource associated with the mood
     */
    public int getColorResource(){
        return this.moodType.getColorCode();
    }

    /**
     * Gets the emoticon resource of the mood
     * @return
     *      The emoticon resource of the mood
     */
    public int getEmoticonResource(){
        return this.moodType.getEmoticonResId();
    }

    /**
     * Gets the marker resource of the mood
     * @return
     *      The marker resource of the mood
     */
    public int getMarkerResource(){
        return this.moodType.getMarkerResId();
    }

    /**
     * This compares two objects to see if they're the same
     * @param obj
     *      The object to compare to
     */
    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MoodEvent other = (MoodEvent)obj;

        if (!this.date.equals(other.date)){
            return false;
        }

        if(!this.reason.equals(other.reason)){
            return false;
        }

        if (!this.emotionalState.equals(other.emotionalState)){
            return false;
        }

        if (this.trigger == null){
            if (this.trigger != other.trigger){ // if both are null
                return false;
            }
        } else {
            if (!this.trigger.equals(other.trigger)){
                return false;
            }
        }

        if (this.socialSituation == null){
            if (this.socialSituation != other.socialSituation){
                return false;
            }
        } else {
            if (!this.socialSituation.equals(other.socialSituation)){
                return false;
            }
        }

        return true;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    /**
     * This compares two objects to see what order they should come in, in reverse chronological order
     * @param o
     *      The object to compare to
     */
    @Override
    public int compareTo(MoodEvent o) {
        if (o == null){
            throw new NullPointerException("MoodEvent you're comparing to is null");
        }

        return -1 * this.date.compareTo(o.date); // reverse chronological
    }

}
