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

package com.example.projectapp.models;

import android.net.Uri;

import com.example.projectapp.R;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * This is a class that models a MoodEvent.
 */
public class MoodEvent implements Comparable<MoodEvent>, Serializable {

    public static final String[] ALL_SITUATIONS = {
            "Alone", "With one other person", "With two to several people", "With a crowd"
    };

    private Date date;
    private String emotionalState;
    private String userId;
    private String reason;
    private String socialSituation;
    private MoodType moodType;
    private double latitude; // New: Store latitude
    private double longitude; // New: Store longitude
    private boolean isPublic;
    private String photoUri;
    private String username;
    private ArrayList<Comment> comments;

    // Public no-argument constructor required for Firestore deserialization
    public MoodEvent() {
        // Firestore uses this constructor to deserialize documents.
    }

    /**
     * This is one constructor for the MoodEvent class.
     * @param emotionalState The specific emotional state of the event.
     * @param reason The reason for the event.
     * @param socialSituation The social situation of the event.
     * @param photoUri The URI of the photo associated with the event.
     * @param userId The ID of the user who created the event.
     */
    public MoodEvent(String emotionalState, String reason, String socialSituation,
                     Uri photoUri, String userId) {
        if (reason != null && !validReason(reason)) {
            throw new IllegalArgumentException("Not a valid reason");
        }
        if (MoodType.fromString(emotionalState) == null) {
            throw new IllegalArgumentException("Not a valid emotional state");
        }
        if (socialSituation != null &&
                !Arrays.asList(ALL_SITUATIONS).contains(socialSituation)) {
            throw new IllegalArgumentException("Not a valid social situation");
        }
        if (userId == null){
            throw new IllegalArgumentException("Cannot have null uid");
        }
        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.reason = reason;
        this.socialSituation = socialSituation;
        this.moodType = MoodType.fromString(emotionalState);
        this.photoUri = (photoUri != null) ? photoUri.toString() : null;
        this.userId = userId;
        this.comments = new ArrayList<>();
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment c){
        comments.add(c);
    }

    /**
     * Returns the emotional state of the event.
     * @return The emotional state.
     */
    public String getEmotionalState() {
        return emotionalState;
    }

    /**
     * Sets the emotional state of the event and resets the MoodType.
     * @param emotionalState The emotional state to set.
     */
    public void setEmotionalState(String emotionalState) {
        if (MoodType.fromString(emotionalState) == null) {
            throw new IllegalArgumentException("Not a valid emotional state");
        }
        this.emotionalState = emotionalState;
        this.moodType = MoodType.fromString(emotionalState);
    }

    /**
     * Returns the user ID of the event's creator.
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the username of the event's creator.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the event's creator.
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the user ID of the event's creator.
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the date for testing purposes
     * @param date
     */
    public void setDate(Date date){
        this.date = date;
    }

    /**
     * Returns the reason for the event.
     * @return The reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason for the event.
     * @param reason The reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the social situation of the event.
     * @return The social situation.
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    @PropertyName("public")
    public boolean isPublic() {
        return isPublic;
    }

    @PropertyName("public")
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Sets the social situation of the event.
     * @param socialSituation The social situation to set.
     */
    public void setSocialSituation(String socialSituation) {
        if (socialSituation == null) {
            this.socialSituation = null;
        } else if (socialSituation.equals("Choose not to answer")) {
            this.socialSituation = null;
        } else if (!Arrays.asList(ALL_SITUATIONS).contains(socialSituation)) {
            throw new IllegalArgumentException("Not a valid Social Situation.");
        } else {
            this.socialSituation = socialSituation;
        }
    }

    /**
     * Returns the date of the event.
     * @return The event date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Checks whether the given reason is valid (non-null, non-empty, at most 200 characters).
     * @param reason The reason to validate.
     * @return True if valid; false otherwise.
     */
    public static boolean validReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }
        return reason.length() <= 200;
    }

    /**
     * Returns the latitude of the event location.
     * @return The latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of the event location.
     * @param latitude The latitude to set.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude of the event location.
     * @return The longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of the event location.
     * @param longitude The longitude to set.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the photo URI associated with the event.
     * @return The photo URI.
     */
    
    @PropertyName("photoUri")
    public String getPhotoUriRaw() {
        return photoUri;
    }

    @PropertyName("photoUri")
    public void setPhotoUriRaw(String photoUri) {
        this.photoUri = photoUri;
    }

    // Convenience getter and setter for code usage
    @Exclude
    public Uri getPhotoUri() {
        return (photoUri != null) ? Uri.parse(photoUri) : null;
    }

    @Exclude
    public void setPhotoUri(Uri photoUri) {
        this.photoUri = (photoUri != null) ? photoUri.toString() : null;
    }

    /**
     * Returns the color resource associated with the event's mood.
     * This method is excluded from Firestore serialization.
     * @return The color resource.
     */
    @Exclude
    public int getColorResource() {
        if (this.moodType == null) {
            return R.color.happiness;
        }
        return this.moodType.getColorCode();
    }

    /**
     * Returns the emoticon resource associated with the event's mood.
     * This method is excluded from Firestore serialization.
     * @return The emoticon resource.
     */
    @Exclude
    public int getEmoticonResource() {
        if (this.moodType == null) {
            return 0;
        }
        return this.moodType.getEmoticonResId();
    }

    /**
     * Returns the marker resource associated with the event's mood.
     * This method is excluded from Firestore serialization.
     * @return The marker resource.
     */
    @Exclude
    public int getMarkerResource() {
        if (this.moodType == null) {
            return 0;
        }
        return this.moodType.getMarkerResId();
    }

    /**
     * Compares this MoodEvent with another for reverse chronological order.
     * @param o The MoodEvent to compare with.
     * @return A negative integer, zero, or a positive integer as this object is less than,
     * equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(MoodEvent o) {
        if (o == null) {
            throw new NullPointerException("MoodEvent you're comparing to is null");
        }
        return -1 * this.date.compareTo(o.date);
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }

        if (o == null){
            return false;
        }

        MoodEvent m = (MoodEvent)o;

        boolean reasonSame = this.reason != null && this.reason.equals(m.reason) || this.reason == m.reason;
        boolean situationSame =  this.socialSituation != null && this.socialSituation.equals(m.socialSituation) || this.socialSituation == m.socialSituation;
        boolean emotionalStateSame = this.emotionalState.equals(m.emotionalState);
        boolean uidSame = this.userId.equals(m.userId);
        boolean locationSame = this.latitude == m.latitude && this.longitude == m.longitude;
        boolean dateSame = this.date.equals(m.date);

        return reasonSame && situationSame && emotionalStateSame && uidSame && locationSame && dateSame;

    }


}
