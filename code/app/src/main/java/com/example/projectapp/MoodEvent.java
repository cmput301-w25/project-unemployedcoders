// -----------------------------------------------------------------------------
// File: MoodEvent.java
// -----------------------------------------------------------------------------
// This file defines the MoodEvent class, a model class representing a single
// mood-related event in the ProjectApp. It captures details such as the current
// date/time, a required emotional state, an optional reason (no more than 200 characters
// and 3 words), an optional social situation, and a flag indicating if the event is public.
// The class implements Comparable for sorting events in reverse chronological order
// and Serializable for potential data persistence.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;

public class MoodEvent implements Comparable<MoodEvent>, Serializable {

    public static final String[] ALL_SITUATIONS = {
            "Alone",
            "With one other person",
            "With two to several people",
            "With a crowd"
    };

    private Date date;
    private String emotionalState;
    private String reason;
    private String socialSituation;
    private MoodType moodType;
    private double latitude;  // Stored latitude
    private double longitude; // Stored longitude
    private boolean isPublic; // True if the event is public

    private File imageFile;

    private String uid;



    /**
     * Constructor that accepts emotional state, reason, social situation, and a public flag.
     *
     * @param emotionalState The specific emotional state of the event.
     * @param reason         The reason for the event (<=200 characters and <=3 words).
     * @param socialSituation The social situation of the event (must be one of ALL_SITUATIONS or null).
     * @param isPublic       True if the event is public; false if private.
     */
    public MoodEvent(String emotionalState, String reason, String socialSituation, boolean isPublic) {
        if (!validReason(reason)) {
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
        this.isPublic = isPublic;
    }

    /**
     * Constructor that accepts only emotional state and reason.
     * Social situation defaults to null and isPublic defaults to false.
     *
     * @param emotionalState The emotional state.
     * @param reason         The reason.
     */
    public MoodEvent(String emotionalState, String reason) {
        this(emotionalState, reason, null, false);
    }

    /**
     * Empty constructor required for Firebase deserialization.
     */
    public MoodEvent() {}

    // Getters and setters

    public String getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(String emotionalState) {
        if (MoodType.fromString(emotionalState) == null) {
            throw new IllegalArgumentException("Not a valid emotional state");
        }
        this.emotionalState = emotionalState;
        this.moodType = MoodType.fromString(emotionalState);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        if (!validReason(reason)) {
            throw new IllegalArgumentException("Not a valid reason");
        }
        this.reason = reason;
    }

    public String getSocialSituation() {
        return socialSituation;
    }
    public String getuid(){
        return uid;
    }

    public void setSocialSituation(String socialSituation) {
        if (socialSituation == null || socialSituation.equals("Choose not to answer")) {
            this.socialSituation = null;
        } else {
            if (!Arrays.asList(ALL_SITUATIONS).contains(socialSituation)) {
                throw new IllegalArgumentException("Not a valid social situation");
            }
            this.socialSituation = socialSituation;
        }
    }

    public Date getDate() {
        return date;
    }

    /**
     * Validates the reason text to ensure it is no more than 200 characters and no more than 3 words.
     *
     * @param reason The reason text.
     * @return True if valid, false otherwise.
     */
    public static boolean validReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }
        int wordCount = reason.trim().split("\\s+").length;
        return reason.length() <= 200 && wordCount <= 3;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getColorResource() {
        return this.moodType.getColorCode();
    }

    public int getEmoticonResource() {
        return this.moodType.getEmoticonResId();
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public File getPhotoUrl() {
        return imageFile;
    }

    public void setPhotoUrl(File imageFile) {
        this.imageFile = imageFile;
    }

    /**
     * Checks equality by comparing date, reason, emotional state, social situation, and public flag.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MoodEvent other = (MoodEvent) obj;
        if (!this.date.equals(other.date))
            return false;
        if (!this.reason.equals(other.reason))
            return false;
        if (!this.emotionalState.equals(other.emotionalState))
            return false;
        if (this.socialSituation == null) {
            if (other.socialSituation != null)
                return false;
        } else if (!this.socialSituation.equals(other.socialSituation))
            return false;
        return this.isPublic == other.isPublic;
    }

    /**
     * Compares two MoodEvent objects for reverse chronological order.
     */
    @Override
    public int compareTo(MoodEvent o) {
        if (o == null) {
            throw new NullPointerException("MoodEvent you're comparing to is null");
        }
        return -1 * this.date.compareTo(o.date); // reverse chronological order
    }
}
