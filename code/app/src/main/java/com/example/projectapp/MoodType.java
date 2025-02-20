package com.example.projectapp;

public enum MoodType {
    ANGER("Anger", "#F31515", R.string.anger_emoticon),
    CONFUSION("Confusion", "#F315C3", R.string.confusion_emoticon),
    DISGUST("Disgust", "#CD8106", R.string.disgust_emoticon),
    FEAR("Fear", "#15F3F3", R.string.fear_emoticon),
    HAPPINESS("Happiness", "#FFCD27", R.string.happiness_emoticon),
    SADNESS("Sadness", "#4B68F4", R.string.sadness_emoticon),
    SHAME("Shame", "#4BD83E", R.string.shame_emoticon),
    SURPRISE("Surprise", "#FB9835", R.string.surprise_emoticon);

    private final String displayName;
    private final String colorCode;
    private final int emoticonResId; // Now refers to a string resource

    MoodType(String displayName, String colorCode, int emoticonResId) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.emoticonResId = emoticonResId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
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

