package com.example.projectapp;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * This is a class that models a MoodEvent
 */
public class MoodEvent implements Comparable<MoodEvent> {


    public static final String[] ALL_MOODS = {"Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"};
    public static final String[] ALL_SITUATIONS = {"Alone" , "With one other person", "With two to several people", "With a crowd"};

    private Date date;
    private String emotionalState;
    private String trigger;
    private String socialSituation;


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
        if (!validTrigger(this.trigger)){
            throw new IllegalArgumentException("Not a valid trigger");
        }

        if (!Arrays.asList(ALL_MOODS).contains(emotionalState)){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        if (!Arrays.asList(ALL_SITUATIONS).contains(socialSituation)){
            throw new IllegalArgumentException("Not a valid social situation");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.trigger = trigger;
        this.socialSituation = socialSituation;

    }

    /**
     * This is one constructor for the MoodEvent class
     * @param emotionalState
     *      The specific emotional state of the event
     */
    public MoodEvent(String emotionalState){
        if (!Arrays.asList(ALL_MOODS).contains(emotionalState)){
            throw new IllegalArgumentException("Not a valid emotional state");
        }

        this.emotionalState = emotionalState;
        this.date = Calendar.getInstance().getTime();
        this.trigger = null;
        this.socialSituation = null;

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
        this.emotionalState = emotionalState;
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
        int wordCount = trigger.trim().split(" ").length;
        return wordCount <= 3 && trigger.length() <= 20;
    }

    /**
     * Gets the color resource associated with the mood
     * @return
     *      The color resource associated with the mood
     */
    public int getColorResource(){
        if (getEmotionalState().equals("Anger")){
            return R.color.anger;
        } else if (getEmotionalState().equals("Confusion")){
            return R.color.confusion;
        } else if (getEmotionalState().equals("Disgust")){
            return R.color.disgust;
        } else if (getEmotionalState().equals("Fear")){
            return R.color.fear;
        } else if (getEmotionalState().equals("Happiness")){
            return R.color.happiness;
        } else if (getEmotionalState().equals("Sadness")){
            return R.color.sadness;
        } else if (getEmotionalState().equals("Shame")){
            return R.color.shame;
        } else if (getEmotionalState().equals("Surprise")){
            return R.color.surprise;
        } else {
            return R.color.white;
        }
    }

    /**
     * Gets the emoticon resource of the mood
     * @return
     *      The emoticon resource of the mood
     */
    public int getEmoticonResource(){
        if (getEmotionalState().equals("Anger")){
            return R.string.anger_emoticon;
        } else if (getEmotionalState().equals("Confusion")){
            return R.string.shame_emoticon;
        } else if (getEmotionalState().equals("Disgust")){
            return R.string.disgust_emoticon;
        } else if (getEmotionalState().equals("Fear")){
            return R.string.fear_emoticon;
        } else if (getEmotionalState().equals("Happiness")){
            return R.string.happiness_emoticon;
        } else if (getEmotionalState().equals("Sadness")){
            return R.string.sadness_emoticon;
        } else if (getEmotionalState().equals("Shame")){
            return R.string.shame_emoticon;
        } else if (getEmotionalState().equals("Surprise")){
            return R.string.surprise_emoticon;
        } else {
            return 0;
        }
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
            if (this.trigger != other.trigger){
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
