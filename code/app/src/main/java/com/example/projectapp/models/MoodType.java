// -----------------------------------------------------------------------------
// File: MoodType.java
// -----------------------------------------------------------------------------
// This file defines the MoodType class, which likely represents a model for
// different mood states or categories within the ProjectApp. It may be used
// to store or manage mood-related data.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:

// -----------------------------------------------------------------------------
package com.example.projectapp.models;

import com.example.projectapp.R;

/**
 * An enumeration to keep the information for emotional states together
 */
public enum MoodType {
    ANGER("Anger", R.color.anger, R.string.anger_emoticon, R.drawable.anger_marker),
    CONFUSION("Confusion", R.color.confusion, R.string.confusion_emoticon, R.drawable.confusion_marker),
    DISGUST("Disgust", R.color.disgust, R.string.disgust_emoticon, R.drawable.disgust_marker),
    FEAR("Fear", R.color.fear, R.string.fear_emoticon, R.drawable.fear_marker),
    HAPPINESS("Happiness", R.color.happiness, R.string.happiness_emoticon, R.drawable.happiness_marker),
    SADNESS("Sadness", R.color.sadness, R.string.sadness_emoticon, R.drawable.sadness_marker),
    SHAME("Shame", R.color.shame, R.string.shame_emoticon, R.drawable.shame_marker),
    SURPRISE("Surprise", R.color.surprise, R.string.surprise_emoticon, R.drawable.surprise_marker);

    private final String displayName;
    private final int colorCode;
    private final int emoticonResId; // Now refers to a string resource
    private final int markerResId;

    /**
     * Constructor for MoodType enum
     * @param displayName
     *      display name of emotional state
     * @param colorCode
     *      color code of emotional state
     * @param emoticonResId
     *      emoticon resource id of emotional state
     */
    MoodType(String displayName, int colorCode, int emoticonResId, int markerResId) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.emoticonResId = emoticonResId;
        this.markerResId = markerResId;
    }

    /**
     * Gets the emotional state's display name
     * @return
     *      the emotional state's display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the emotional state's color code
     * @return
     *      the emotional state's color code
     */
    public int getColorCode() {
        return colorCode;
    }

    /**
     * Gets the emotional state's emoticon resource id
     * @return
     *      the emotional state's emoticon resource id
     */
    public int getEmoticonResId() {
        return emoticonResId;
    }

    /**
     * Gets the emotional state's marker resource id
     * @return
     *      the emotional state's emoticon resource id
     */
    public int getMarkerResId() {
        return markerResId;
    }

    /**
     * Utility method to convert a string (from the spinner) to a MoodType
     * @param moodName
     *      the emotional state of the mood
     * @return
     *      the mood type corresponding to the mood name
     */
    public static MoodType fromString(String moodName) {
        for (MoodType mood : MoodType.values()) {
            if (mood.getDisplayName().equalsIgnoreCase(moodName)) {
                return mood;
            }
        }
        return null;
    }
}

