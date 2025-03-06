package com.example.projectapp;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * This is a class that models a MoodEvent
 */
public class MoodEvent implements Comparable<MoodEvent> {


    public static final String[] ALL_SITUATIONS = {"Alone" , "With one other person", "With two to several people", "With a crowd"};

    private Date date;
    private String emotionalState;
    private String trigger;
    private String socialSituation;

    private MoodType moodType;
    private double latitude;  // New: Store latitude
    private double longitude; // New: Store longitude


    /**
     * This is one constructor for the MoodEvent class
     * @param emotionalState
     *      The specific emotional state of the event
     * @param trigger
     *      The trigger of the event
     * @param socialSituation
     *      The social situation of the event
     */
    public MoodEvent(String emotionalState, String trigger, String socialSituation){
        if (!validTrigger(trigger)){
            throw new IllegalArgumentException("Not a valid trigger");
        }

        if (MoodType.fromString(emotionalState) == null){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        if (!Arrays.asList(ALL_SITUATIONS).contains(socialSituation) && socialSituation != null){
            throw new IllegalArgumentException("Not a valid social situation");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.trigger = trigger;

        this.socialSituation = socialSituation;
        this.moodType = MoodType.fromString(emotionalState);

    }

    /**
     * This is one constructor for the MoodEvent class
     * @param emotionalState
     *      The specific emotional state of the event
     */
    public MoodEvent(String emotionalState){
        if (MoodType.fromString(emotionalState) == null){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
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
     * This returns the trigger of the event
     * @return
     *      Returns the emotional state of the event
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * This sets the trigger of the event
     * @param trigger
     *      The trigger to set for the event
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
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
        this.socialSituation = socialSituation;
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
     * @param trigger
     *      The trigger to check
     * @return
     *      Whether or not the trigger is valid
     */
    public static boolean validTrigger(String trigger){
        if (trigger == null){
            return true;
        }

        int wordCount = trigger.trim().split(" ").length;
        return wordCount <= 3 && trigger.length() <= 20;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
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
