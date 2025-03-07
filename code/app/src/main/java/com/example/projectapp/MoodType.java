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
package com.example.projectapp;

public enum MoodType {
    ANGER("Anger", R.color.anger, R.string.anger_emoticon),
    CONFUSION("Confusion", R.color.confusion, R.string.confusion_emoticon),
    DISGUST("Disgust", R.color.disgust, R.string.disgust_emoticon),
    FEAR("Fear", R.color.fear, R.string.fear_emoticon),
    HAPPINESS("Happiness", R.color.happiness, R.string.happiness_emoticon),
    SADNESS("Sadness", R.color.sadness, R.string.sadness_emoticon),
    SHAME("Shame", R.color.shame, R.string.shame_emoticon),
    SURPRISE("Surprise", R.color.surprise, R.string.surprise_emoticon);

    private final String displayName;
    private final int colorCode;
    private final int emoticonResId; // Now refers to a string resource

    MoodType(String displayName, int colorCode, int emoticonResId) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.emoticonResId = emoticonResId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorCode() {
        return colorCode;
    }

    public int getEmoticonResId() {
        return emoticonResId;
    }

    // Utility method to convert a string (from the spinner) to a MoodType
    public static MoodType fromString(String moodName) {
        for (MoodType mood : MoodType.values()) {
            if (mood.getDisplayName().equalsIgnoreCase(moodName)) {
                return mood;
            }
        }
        return null;
    }
}

