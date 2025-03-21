package com.example.projectapp;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * This is a class that models a MoodEvent
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
    private double latitude;
    private double longitude;
    // We'll store it as "public" in Firestore:
    private boolean isPublic;
    private Uri photoUri;

    // Public no-argument constructor required for Firestore
    public MoodEvent() {
        // Firestore uses this constructor
        // If you want to provide a fallback for moodType, you can do it here too
    }

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
        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.reason = reason;
        this.socialSituation = socialSituation;
        this.moodType = MoodType.fromString(emotionalState);
        this.photoUri = photoUri;
        this.userId = userId;
    }

    // Additional constructors if needed...
    // e.g. MoodEvent(String emotionalState, String reason, ...)

    // Ensure moodType is re-set if you ever change emotionalState
    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
        this.moodType = MoodType.fromString(emotionalState);
    }

    public String getEmotionalState() {
        return emotionalState;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSocialSituation() {
        return socialSituation;
    }
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public Date getDate() {
        return date;
    }

    @PropertyName("public")
    public boolean isPublic() {
        return isPublic;
    }

    @PropertyName("public")
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public static boolean validReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }
        return (reason.length() <= 200);
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

    public Uri getPhotoUri() {
        return photoUri;
    }


    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    // Exclude these from Firestore serialization, so it won't call them
    // Also add a null check for moodType
    @Exclude
    public int getColorResource() {
        if (this.moodType == null) {
            // Provide fallback color
            return R.color.happiness;
        }
        return this.moodType.getColorCode();
    }

    @Exclude
    public int getEmoticonResource() {
        if (this.moodType == null) {
            // fallback or 0
            return 0;
        }
        return this.moodType.getEmoticonResId();
    }

    @Exclude
    public int getMarkerResource() {
        if (this.moodType == null) {
            // fallback or 0
            return 0;
        }
        return this.moodType.getMarkerResId();
    }

    @Override
    public int compareTo(MoodEvent o) {
        if (o == null) {
            throw new NullPointerException("Comparing to null MoodEvent");
        }
        // sort reverse-chronological by date
        return -1 * this.date.compareTo(o.date);
    }
}
